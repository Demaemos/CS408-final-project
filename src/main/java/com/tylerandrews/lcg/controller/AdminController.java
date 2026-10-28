package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.form.AdminGameForm;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
public class AdminController {

    private final GameService gameService;
    private final GameRepository gameRepository;

    public AdminController(GameService gameService, GameRepository gameRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("games", gameService.allGames());
        return "admin/dashboard";
    }

    @GetMapping("/admin/games/new")
    public String newGame(Model model) {
        model.addAttribute("gameForm", new AdminGameForm());
        prepareForm(model);
        return "admin/game-form";
    }

    @PostMapping("/admin/games")
    public String createGame(@Valid @ModelAttribute("gameForm") AdminGameForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        validateSlug(form, null, result);
        if (result.hasErrors()) {
            prepareForm(model);
            return "admin/game-form";
        }

        Game game = new Game();
        copyForm(form, game);
        gameService.save(game, form.getGenreIds(), form.getPlatformIds());
        redirect.addFlashAttribute("message", "Game created.");
        return "redirect:/admin";
    }

    @GetMapping("/admin/games/{id}/edit")
    public String editGame(@PathVariable Long id, Model model) {
        Game game = findGame(id);
        model.addAttribute("gameId", id);
        model.addAttribute("gameForm", toForm(game));
        prepareForm(model);
        return "admin/game-form";
    }

    @PostMapping("/admin/games/{id}")
    public String updateGame(@PathVariable Long id,
                             @Valid @ModelAttribute("gameForm") AdminGameForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        Game game = findGame(id);
        validateSlug(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("gameId", id);
            prepareForm(model);
            return "admin/game-form";
        }

        copyForm(form, game);
        gameService.save(game, form.getGenreIds(), form.getPlatformIds());
        redirect.addFlashAttribute("message", "Game updated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/games/{id}/delete")
    public String deleteGame(@PathVariable Long id, RedirectAttributes redirect) {
        gameService.delete(findGame(id));
        redirect.addFlashAttribute("message", "Game deleted.");
        return "redirect:/admin";
    }

    private Game findGame(Long id) {
        return gameService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private void validateSlug(AdminGameForm form, Long currentId, BindingResult result) {
        gameRepository.findBySlug(form.getSlug())
            .filter(existing -> !Objects.equals(existing.getId(), currentId))
            .ifPresent(existing -> result.rejectValue("slug", "taken", "That slug is already in use."));
    }

    private void prepareForm(Model model) {
        model.addAttribute("genres", gameService.genres());
        model.addAttribute("platforms", gameService.platforms());
        model.addAttribute("statuses", Game.ReleaseStatus.values());
    }

    private void copyForm(AdminGameForm form, Game game) {
        game.setTitle(form.getTitle().trim());
        game.setSlug(form.getSlug().trim());
        game.setTagline(form.getTagline());
        game.setDescription(form.getDescription());
        game.setReleaseStatus(form.getReleaseStatus());
        game.setReleaseDate(form.getReleaseDate());
        game.setEsrbRating(form.getEsrbRating());
        game.setCoverImage(form.getCoverImage());
        game.setTrailerUrl(form.getTrailerUrl());
    }

    private AdminGameForm toForm(Game game) {
        AdminGameForm form = new AdminGameForm();
        form.setTitle(game.getTitle());
        form.setSlug(game.getSlug());
        form.setTagline(game.getTagline());
        form.setDescription(game.getDescription());
        form.setReleaseStatus(game.getReleaseStatus());
        form.setReleaseDate(game.getReleaseDate());
        form.setEsrbRating(game.getEsrbRating());
        form.setCoverImage(game.getCoverImage());
        form.setTrailerUrl(game.getTrailerUrl());
        form.setGenreIds(game.getGenres().stream().map(genre -> genre.getId())
            .collect(java.util.stream.Collectors.toSet()));
        form.setPlatformIds(game.getPlatforms().stream().map(platform -> platform.getId())
            .collect(java.util.stream.Collectors.toSet()));
        return form;
    }
}
