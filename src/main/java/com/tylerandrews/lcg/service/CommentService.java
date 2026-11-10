package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> findByGame(Game game) {
        return commentRepository.findByGameOrderByCreatedAtAsc(game);
    }

    public List<Comment> findByNewsPost(NewsPost newsPost) {
        return commentRepository.findByNewsPostOrderByCreatedAtAsc(newsPost);
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

    public Comment update(Comment comment, String body) {
        comment.setBody(body == null ? null : body.trim());
        comment.setEditedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    public boolean canManage(User currentUser, Comment comment) {
        if (currentUser == null || comment == null || comment.getUser() == null) {
            return false;
        }
        return currentUser.getRole() == User.Role.ADMIN || comment.getUser().getId().equals(currentUser.getId());
    }
}
