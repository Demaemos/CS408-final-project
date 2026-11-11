package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentModerationService moderationService;

    public CommentService(CommentRepository commentRepository, CommentModerationService moderationService) {
        this.commentRepository = commentRepository;
        this.moderationService = moderationService;
    }

    public List<Comment> findByGame(Game game) {
        return organizeThreads(commentRepository.findByGameOrderByCreatedAtAsc(game));
    }

    public List<Comment> findByNewsPost(NewsPost newsPost) {
        return organizeThreads(commentRepository.findByNewsPostOrderByCreatedAtAsc(newsPost));
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createGameComment(User user, Game game, String body) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setGame(game);
        comment.setBody(body == null ? null : body.trim());
        return commentRepository.save(comment);
    }

    public Comment createNewsComment(User user, NewsPost newsPost, String body) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setNewsPost(newsPost);
        comment.setBody(body == null ? null : body.trim());
        return commentRepository.save(comment);
    }

    public Comment createGameReply(User user, Game game, Comment parent, String body) {
        Comment comment = newComment(user, body);
        comment.setGame(game);
        comment.setParent(parent);
        return commentRepository.save(comment);
    }

    public Comment createNewsReply(User user, NewsPost newsPost, Comment parent, String body) {
        Comment comment = newComment(user, body);
        comment.setNewsPost(newsPost);
        comment.setParent(parent);
        return commentRepository.save(comment);
    }

    public Comment update(Comment comment, String body) {
        comment.setBody(body == null ? null : body.trim());
        comment.setEditedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Comment comment) {
        for (Comment reply : commentRepository.findByParentOrderByCreatedAtAsc(comment)) {
            delete(reply);
        }
        commentRepository.delete(comment);
    }

    public boolean containsInappropriateLanguage(String body) {
        return moderationService.containsInappropriateLanguage(body);
    }

    public boolean canEdit(User currentUser, Comment comment) {
        return currentUser != null && comment != null && comment.getUser() != null
            && currentUser.getRole() != User.Role.ADMIN
            && comment.getUser().getId().equals(currentUser.getId());
    }

    public boolean canDelete(User currentUser, Comment comment) {
        if (currentUser == null || comment == null || comment.getUser() == null) {
            return false;
        }
        return currentUser.getRole() == User.Role.ADMIN || comment.getUser().getId().equals(currentUser.getId());
    }

    private Comment newComment(User user, String body) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setBody(body == null ? null : body.trim());
        return comment;
    }

    private List<Comment> organizeThreads(List<Comment> comments) {
        Map<Long, Comment> byId = new HashMap<>();
        for (Comment comment : comments) {
            comment.getReplies().clear();
            byId.put(comment.getId(), comment);
        }
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : comments) {
            Comment parent = comment.getParent();
            Comment loadedParent = parent == null ? null : byId.get(parent.getId());
            if (loadedParent == null) {
                roots.add(comment);
            } else {
                loadedParent.getReplies().add(comment);
            }
        }
        return roots;
    }
}
