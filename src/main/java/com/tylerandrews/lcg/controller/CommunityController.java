package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.service.CommentService;
import com.tylerandrews.lcg.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CommunityController {

    private final CommentService commentService;
    private final GameService gameService;

    public CommunityController(CommentService commentService, GameService gameService) {
        this.commentService = commentService;
        this.gameService = gameService;
    }

    @GetMapping("/community")
    public String community(@RequestParam(required = false) Long game, Model model) {
        Game selectedGame = game == null ? null : gameService.findById(game)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        model.addAttribute("games", gameService.allGames());
        model.addAttribute("selectedGame", game);
        model.addAttribute("communityPosts", commentService.communityPosts(selectedGame));
        return "community";
    }
}
