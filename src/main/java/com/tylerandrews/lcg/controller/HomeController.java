package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.service.GameService;
import com.tylerandrews.lcg.service.NewsPostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final GameService gameService;
    private final NewsPostService newsPostService;

    public HomeController(GameService gameService, NewsPostService newsPostService) {
        this.gameService = gameService;
        this.newsPostService = newsPostService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredGames", gameService.allGames().stream().limit(2).toList());
        model.addAttribute("latestNews", newsPostService.latest(3));
        return "home";
    }
}