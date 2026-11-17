package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.form.SupportTicketForm;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final GameRepository gameRepository;
    private final SupportTicketService ticketService;

    public ContactController(GameRepository gameRepository, SupportTicketService ticketService) {
        this.gameRepository = gameRepository;
        this.ticketService = ticketService;
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        if (!model.containsAttribute("ticketForm")) {
            model.addAttribute("ticketForm", new SupportTicketForm());
        }
        addGames(model);
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("ticketForm") SupportTicketForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Game game = null;
        if (form.getGameId() != null) {
            game = gameRepository.findById(form.getGameId()).orElse(null);
            if (game == null) {
                result.rejectValue("gameId", "notFound", "Choose a game from the list.");
            }
        }
        if (result.hasErrors()) {
            addGames(model);
            return "contact";
        }
        ticketService.submit(form, game);
        redirect.addFlashAttribute("ticketSubmitted", true);
        return "redirect:/contact";
    }

    private void addGames(Model model) {
        model.addAttribute("games", gameRepository.findAll());
    }
}
