package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.repository.TeamMemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    private final TeamMemberRepository teamMemberRepository;

    public AboutController(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("team", teamMemberRepository.findAllByOrderBySortOrderAsc());
        return "about";
    }
}