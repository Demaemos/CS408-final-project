package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.form.CommentForm;
import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.service.CommentService;
import com.tylerandrews.lcg.service.NewsPostService;
import com.tylerandrews.lcg.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
public class NewsController {

    private static final int PAGE_SIZE = 10;

    private final NewsPostService newsPostService;
    private final CommentService commentService;
    private final UserService userService;

    public NewsController(NewsPostService newsPostService,
                         CommentService commentService,
                         UserService userService) {
        this.newsPostService = newsPostService;
        this.commentService = commentService;
        this.userService = userService;
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
        model.addAttribute("comments", commentService.findByNewsPost(post));
        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", new CommentForm());
        }
        return "news-detail";
    }

    @PostMapping("/news/{slug}/comments")
    public String addComment(@PathVariable String slug,
                             @Valid @ModelAttribute("commentForm") CommentForm form,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirect) {
        NewsPost post = newsPostService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        if (result.hasErrors()) {
            model.addAttribute("post", post);
            model.addAttribute("comments", commentService.findByNewsPost(post));
            return "news-detail";
        }
        if (commentService.containsInappropriateLanguage(form.getBody())) {
            redirect.addFlashAttribute("commentMessage", "Your comment was removed because it contains language not allowed on this site.");
            return "redirect:/news/" + slug;
        }
        commentService.createNewsComment(currentUser.get(), post, form.getBody());
        redirect.addFlashAttribute("commentMessage", "Comment posted.");
        return "redirect:/news/" + slug;
    }

    @PostMapping("/news/{slug}/comments/{commentId}/replies")
    public String addReply(@PathVariable String slug,
                           @PathVariable Long commentId,
                           @Valid @ModelAttribute("commentForm") CommentForm form,
                           BindingResult result,
                           HttpSession session,
                           RedirectAttributes redirect) {
        NewsPost post = newsPostService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment parent = commentService.findById(commentId)
            .filter(comment -> belongsToNewsPost(comment, post))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (result.hasErrors()) {
            redirect.addFlashAttribute("commentMessage", "Replies must contain 1 to 2000 characters.");
            return "redirect:/news/" + slug;
        }
        if (commentService.containsInappropriateLanguage(form.getBody())) {
            redirect.addFlashAttribute("commentMessage", "Your reply was removed because it contains language not allowed on this site.");
            return "redirect:/news/" + slug;
        }
        commentService.createNewsReply(currentUser.get(), post, parent, form.getBody());
        redirect.addFlashAttribute("commentMessage", "Reply posted.");
        return "redirect:/news/" + slug;
    }

    @PostMapping("/news/{slug}/comments/{commentId}/edit")
    public String editComment(@PathVariable String slug,
                              @PathVariable Long commentId,
                              @RequestParam("body") String body,
                              HttpSession session,
                              RedirectAttributes redirect) {
        NewsPost post = newsPostService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment comment = commentService.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!belongsToNewsPost(comment, post)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        if (!commentService.canEdit(currentUser.get(), comment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own comments.");
        }
        if (body == null || body.isBlank() || body.length() > 2000) {
            redirect.addFlashAttribute("commentMessage", "Edited comments must contain 1 to 2000 characters.");
            return "redirect:/news/" + slug;
        }
        if (commentService.containsInappropriateLanguage(body)) {
            commentService.delete(comment);
            redirect.addFlashAttribute("commentMessage", "Your comment was removed because it contains language not allowed on this site.");
            return "redirect:/news/" + slug;
        }
        commentService.update(comment, body);
        redirect.addFlashAttribute("commentMessage", "Comment updated.");
        return "redirect:/news/" + slug;
    }

    @PostMapping("/news/{slug}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable String slug,
                                @PathVariable Long commentId,
                                HttpSession session,
                                RedirectAttributes redirect) {
        NewsPost post = newsPostService.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/account";
        }
        Comment comment = commentService.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!belongsToNewsPost(comment, post)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        if (!commentService.canDelete(currentUser.get(), comment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments.");
        }
        commentService.delete(comment);
        redirect.addFlashAttribute("commentMessage", "Comment deleted.");
        return "redirect:/news/" + slug;
    }

    private Optional<User> currentUser(HttpSession session) {
        Object userId = session.getAttribute(SessionKeys.USER_ID);
        if (!(userId instanceof Long id)) {
            return Optional.empty();
        }
        return userService.findById(id);
    }

    private boolean belongsToNewsPost(Comment comment, NewsPost post) {
        return comment.getNewsPost() != null && Objects.equals(comment.getNewsPost().getId(), post.getId());
    }
}
