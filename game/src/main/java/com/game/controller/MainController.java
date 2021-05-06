package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import com.game.service.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private PlayerService playerService;

    @GetMapping(value = "/rest/players")
    public @ResponseBody List<Player> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize){

        return playerService.getAllPlayers(order, pageNumber, pageSize, createSpec(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel));
    }

    @GetMapping("/rest/players/count")
    public @ResponseBody Integer getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

        return playerService.getPlayersCount(createSpec(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel));
    }

    @PostMapping("/rest/players/")
    public @ResponseBody Player createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }

    @GetMapping("/rest/players/{id}")
    public @ResponseBody Player getPlayer(@PathVariable String id) {
        return playerService.getPlayer(id);
    }

    @PostMapping("/rest/players/{id}")
    public @ResponseBody Player updatePlayer(
            @PathVariable String id,
            @RequestBody Player player){
        return playerService.updatePlayer(id, player);
    }

    @DeleteMapping ("/rest/players/{id}")
    public ResponseEntity<Long> deletePlayer(@PathVariable String id) {
        playerService.deletePlayer(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Specification<Player> createSpec(String name, String title, Race race, Profession profession,
                                            Long after, Long before, Boolean banned, Integer minExperience,
                                            Integer maxExperience,  Integer minLevel,  Integer maxLevel){
        Specification<Player> specs = Specification.where(null);
        if (name != null) {
            specs = specs.and(playerService.nameContains(name));
        }
        if (title != null) {
            specs = specs.and(playerService.titleContains(title));
        }
        if (race != null) {
            specs = specs.and(playerService.raceContains(race));
        }
        if (profession != null) {
            specs = specs.and(playerService.professionContains(profession));
        }
        if (after != null || before !=null) {
            specs = specs.and(playerService.birthdayContains(after, before));
        }
        if (banned != null) {
            specs = specs.and(playerService.bannedContains(banned));
        }
        if (minExperience != null || maxExperience != null) {
            specs = specs.and(playerService.experienceContains(minExperience, maxExperience));
        }
        if (minLevel != null || maxLevel != null) {
            specs = specs.and(playerService.levelContains(minLevel, maxLevel));
        }
        return specs;
    }
}
