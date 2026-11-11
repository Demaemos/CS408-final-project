package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.NewsPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByGameOrderByCreatedAtAsc(Game game);
    List<Comment> findByNewsPostOrderByCreatedAtAsc(NewsPost newsPost);
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
}
