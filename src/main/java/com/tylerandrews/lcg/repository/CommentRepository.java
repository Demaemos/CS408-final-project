package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.Comment;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.NewsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByGameOrderByCreatedAtAsc(Game game);
    List<Comment> findByNewsPostOrderByCreatedAtAsc(NewsPost newsPost);
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

    @Query("""
        select comment from Comment comment
        where (:game is null or comment.game = :game)
        order by comment.createdAt desc
        """)
    Page<Comment> findCommunityPosts(@Param("game") Game game, Pageable pageable);
}
