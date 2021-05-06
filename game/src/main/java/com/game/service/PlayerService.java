package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.InvalidIdException;
import com.game.exceptions.PlayerNotFoundException;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlayerService{

    Player getPlayer(String id) ;
    List<Player> getAllPlayers(PlayerOrder order, Integer pageNumber, Integer pageSize, Specification<Player> spec);
     //List<Player> getAllPlayers(PlayerOrder order, Integer pageNumber, Integer pageSize);
    Integer getPlayersCount( Specification<Player> spec);
    Player createPlayer(Player player);
    Player updatePlayer(String id, Player newPlayer);
    void deletePlayer(String id);

    Specification<Player> nameContains(String name);
    Specification<Player> titleContains(String title);
    Specification<Player> raceContains(Race race);
    Specification<Player> professionContains(Profession profession);
    Specification<Player> experienceContains(Integer min, Integer max);
    Specification<Player> bannedContains(Boolean banned);
    Specification<Player> birthdayContains(Long after, Long before);
    Specification<Player> levelContains(Integer minLevel, Integer maxLevel);
}
