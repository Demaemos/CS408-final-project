package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games")
    public String catalog(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long genre,
                          @RequestParam(required = false) Long platform,
                          @RequestParam(required = false) Game.ReleaseStatus status,
                          Model model) {
        model.addAttribute("games", gameService.search(keyword, genre, platform, status));
        model.addAttribute("genres", gameService.genres());
        model.addAttribute("platforms", gameService.platforms());
        model.addAttribute("statuses", Game.ReleaseStatus.values());

        // Echo the current filters back so the form keeps its values.
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("selectedPlatform", platform);
        model.addAttribute("selectedStatus", status);
        return "games";
    }

    @GetMapping("/games/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Game game = gameService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        model.addAttribute("game", game);
        return "game-detail";
    }
}