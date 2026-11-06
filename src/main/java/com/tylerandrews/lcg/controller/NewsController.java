package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.service.NewsPostService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class NewsController {

    private static final int PAGE_SIZE = 10;

    private final NewsPostService newsPostService;

    public NewsController(NewsPostService newsPostService) {
        this.newsPostService = newsPostService;
    }

    @GetMapping("/news")
    public String news(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String tag,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<NewsPost> newsPage = newsPostService.search(keyword, tag, page, PAGE_SIZE);
        model.addAttribute("posts", newsPage.getContent());
        model.addAttribute("newsPage", newsPage);
        model.addAttribute("tags", newsPostService.tags());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTag", tag);
        return "news";
    }

    @GetMapping("/news/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        NewsPost post = newsPostService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
        model.addAttribute("post", post);
        return "news-detail";
    }
}
