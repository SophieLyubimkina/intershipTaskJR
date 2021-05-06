package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.InvalidIdException;
import com.game.exceptions.PlayerNotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Book;
import java.util.*;

@Service
public class PlayerServiceImpl implements PlayerService{

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    @Transactional
    public List<Player> getAllPlayers(PlayerOrder order, Integer pageNumber, Integer pageSize, Specification<Player> spec){
        Pageable returnPage = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        Page<Player> page = playerRepository.findAll(spec, returnPage );
        if (page.hasContent()) {
            return page.getContent();
        } else {
            return new ArrayList<Player>();
        }
    }

    @Override
    @Transactional
    public Player getPlayer(String id) {

        Long tempId = validateId(id);

        Player returnPlayer = playerRepository.findById(tempId).orElseThrow( ()-> new PlayerNotFoundException());
        return returnPlayer;
    }

    @Override
    public Integer getPlayersCount(Specification<Player> spec ) {
        return Long.valueOf(playerRepository.count(spec)).intValue();
    }

    @Transactional
    @Override
    public Player createPlayer(Player player) {
        validateName(player.getName());
        validateTitle(player.getTitle());
        validateExperience(player.getExperience());
        validateBirthday(player.getBirthday());
        validateRace(player.getRace());
        validateProfession(player.getProfession());

        calculateCurrentLevel(player);
        calculateExpToNextLevel(player);

        if (player.getBanned() == null){
            player.setBanned(false);
        }
        playerRepository.save(player);
        return player;
    }

    @Override
    public Player updatePlayer(String id, Player newPlayer) {
        Player oldPlayer = getPlayer(id);

        if (newPlayer.getName() != null) {
            validateName(newPlayer.getName());
            oldPlayer.setName(newPlayer.getName());
        }

        if (newPlayer.getTitle() != null) {
            validateTitle(newPlayer.getTitle());
            oldPlayer.setTitle(newPlayer.getTitle());
        }

        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }

        if (newPlayer.getExperience() != null) {
            validateExperience(newPlayer.getExperience());
            oldPlayer.setExperience(newPlayer.getExperience());
        }

        if (newPlayer.getBirthday() != null) {
            validateBirthday(newPlayer.getBirthday());
            oldPlayer.setBirthday(newPlayer.getBirthday());
        }

        calculateCurrentLevel(oldPlayer);
        calculateExpToNextLevel(oldPlayer);

        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
        }
        playerRepository.saveAndFlush(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void deletePlayer(String id) {
        Player tempP = getPlayer(id);
        Long tempId = tempP.getId();
        playerRepository.deleteById(tempId);
    }

    public Long validateId(String id){
        Long tempId;
        try {
            tempId = Long.parseLong(id);
            if (tempId <= 0) throw new InvalidIdException();
        } catch (NumberFormatException e) {
            throw new InvalidIdException();
        }
        return tempId;
    }

    public void validateName(String name){
        if ( name == null|| name.isEmpty() || (name.length() > 12))
            throw new InvalidIdException();
    }
    public void validateTitle(String title){
        if ( title == null|| title.isEmpty() || (title.length() > 30))
            throw new InvalidIdException();
    }

    public void validateRace(Race race){
        if (race == null) throw new InvalidIdException();
    }

    public void validateProfession(Profession profession){
        if (profession == null) throw new InvalidIdException();
    }

    public void validateExperience(Integer experience) {
        if (experience == null || experience < 0 || experience > 10000000)
            throw new InvalidIdException();
    }

    public void validateBirthday(Date birthday){
        if (birthday == null) throw new InvalidIdException();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getTime());
        if (calendar.get(Calendar.YEAR) < 2000 || calendar.get(Calendar.YEAR) > 3000)
            throw new InvalidIdException();
    }

    public void calculateCurrentLevel(Player player) {
        double level = (Math.sqrt(2500 + 200*player.getExperience()) - 50) / 100;
        player.setLevel((int)level);
    }

    public void calculateExpToNextLevel(Player player) {
        Integer level = player.getLevel();
        Integer exp = player.getExperience();
        Integer value = 50 * (level + 1) * (level + 2) - exp;
        player.setUntilNextLevel(value);
    }

    //this is specification for filters

    public Specification<Player> nameContains(String name) {

        return (playerRoot, cq, cb) -> cb.like(playerRoot.get("name"), "%" + name + "%");
    }

    public Specification<Player> titleContains(String title) {

        return (playerRoot, cq, cb) -> cb.like(playerRoot.get("title"), "%" + title + "%");
    }

    public Specification<Player> raceContains(Race race) {

        return (playerRoot, cq, cb) -> cb.equal(playerRoot.get("race"), race);
    }

    public Specification<Player> professionContains(Profession profession) {

        return (playerRoot, cq, cb) -> cb.equal(playerRoot.get("profession"), profession);
    }

    public Specification<Player> experienceContains(Integer min, Integer max) {

        if (max == null) return (playerRoot, cq, cb) -> cb.greaterThanOrEqualTo(playerRoot.get("experience"), min);
        if (min == null) return (playerRoot, cq, cb) -> cb.lessThanOrEqualTo(playerRoot.get("experience"), max);
        return (playerRoot, cq, cb) -> cb.between(playerRoot.get("experience"), min, max);
    }

    public Specification<Player> bannedContains(Boolean banned) {

        return (playerRoot, query, cb) -> {
            if (banned) return cb.isTrue(playerRoot.get("banned"));
            return cb.isFalse(playerRoot.get("banned"));};
    }

    public Specification<Player> birthdayContains(Long after, Long before) {
        if (before == null) return  (playerRoot, query, cb) -> cb.greaterThanOrEqualTo(playerRoot.get("birthday"), new Date(after));
        if (after == null)  return (playerRoot, query, cb) -> cb.lessThanOrEqualTo(playerRoot.get("birthday"), new Date(before));

        return (playerRoot, query, cb) -> cb.between(playerRoot.get("birthday"), new Date(after), new Date(before));
    }

    public Specification<Player> levelContains(Integer minLevel, Integer maxLevel) {
        if (minLevel == null)
            return (playerRoot, query, cb) -> cb.lessThanOrEqualTo(playerRoot.get("level"), maxLevel);
        if (maxLevel == null)
            return (playerRoot, query, cb) -> cb.greaterThanOrEqualTo(playerRoot.get("level"), minLevel);

        return (playerRoot, query, cb) -> cb.between(playerRoot.get("level"), minLevel, maxLevel);
    }
}
