package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.form.CommentForm;
import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.service.CommentService;
import com.tylerandrews.lcg.service.GameService;
import com.tylerandrews.lcg.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Objects;

@Controller
public class GameController {

    private final GameService gameService;
    private final CommentService commentService;
    private final UserService userService;

    public GameController(GameService gameService,
                          CommentService commentService,
                          UserService userService) {
        this.gameService = gameService;
        this.commentService = commentService;
        this.userService = userService;
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
        model.addAttribute("comments", commentService.findByGame(game));
        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", new CommentForm());
        }
        return "game-detail";
    }

    @PostMapping("/games/{slug}/comments")
    public String addComment(@PathVariable String slug,
                             @Valid @ModelAttribute("commentForm") CommentForm form,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirect) {
        Game game = gameService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        if (result.hasErrors()) {
            model.addAttribute("game", game);
            model.addAttribute("comments", commentService.findByGame(game));
            return "game-detail";
        }
        if (commentService.containsInappropriateLanguage(form.getBody())) {
            redirect.addFlashAttribute("commentMessage", "Your comment was removed because it contains language not allowed on this site.");
            return "redirect:/games/" + slug;
        }
        commentService.createGameComment(currentUser.get(), game, form.getBody());
        redirect.addFlashAttribute("commentMessage", "Comment posted.");
        return "redirect:/games/" + slug;
    }

    @PostMapping("/games/{slug}/comments/{commentId}/replies")
    public String addReply(@PathVariable String slug,
                           @PathVariable Long commentId,
                           @Valid @ModelAttribute("commentForm") CommentForm form,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes redirect) {
        Game game = gameService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment parent = commentService.findById(commentId)
            .filter(comment -> belongsToGame(comment, game))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (result.hasErrors()) {
            redirect.addFlashAttribute("commentMessage", "Replies must contain 1 to 2000 characters.");
            return "redirect:/games/" + slug;
        }
        if (commentService.containsInappropriateLanguage(form.getBody())) {
            redirect.addFlashAttribute("commentMessage", "Your reply was removed because it contains language not allowed on this site.");
            return "redirect:/games/" + slug;
        }
        commentService.createGameReply(currentUser.get(), game, parent, form.getBody());
        redirect.addFlashAttribute("commentMessage", "Reply posted.");
        return "redirect:/games/" + slug;
    }

    @PostMapping("/games/{slug}/comments/{commentId}/edit")
    public String editComment(@PathVariable String slug,
                              @PathVariable Long commentId,
                              @RequestParam("body") String body,
                              HttpSession session,
                              RedirectAttributes redirect) {
        Game game = gameService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment comment = commentService.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!belongsToGame(comment, game)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        if (!commentService.canEdit(currentUser.get(), comment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own comments.");
        }
        if (body == null || body.isBlank() || body.length() > 2000) {
            redirect.addFlashAttribute("commentMessage", "Edited comments must contain 1 to 2000 characters.");
            return "redirect:/games/" + slug;
        }
        if (commentService.containsInappropriateLanguage(body)) {
            commentService.delete(comment);
            redirect.addFlashAttribute("commentMessage", "Your comment was removed because it contains language not allowed on this site.");
            return "redirect:/games/" + slug;
        }
        commentService.update(comment, body);
        redirect.addFlashAttribute("commentMessage", "Comment updated.");
        return "redirect:/games/" + slug;
    }

    @PostMapping("/games/{slug}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable String slug,
                                @PathVariable Long commentId,
                                HttpSession session,
                                RedirectAttributes redirect) {
        Game game = gameService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment comment = commentService.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!belongsToGame(comment, game)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        if (!commentService.canDelete(currentUser.get(), comment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments.");
        }
        commentService.delete(comment);
        redirect.addFlashAttribute("commentMessage", "Comment deleted.");
        return "redirect:/games/" + slug;
    }

    private Optional<User> currentUser(HttpSession session) {
        Object userId = session.getAttribute(SessionKeys.USER_ID);
        if (!(userId instanceof Long id)) {
            return Optional.empty();
        }
        return userService.findById(id);
    }

    private boolean belongsToGame(Comment comment, Game game) {
        return comment.getGame() != null && Objects.equals(comment.getGame().getId(), game.getId());
    }
}