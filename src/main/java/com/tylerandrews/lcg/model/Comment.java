package com.tylerandrews.lcg.model;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "comments")
public class Comment {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
 
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
 
    @ManyToOne
    @JoinColumn(name = "news_post_id")
    private NewsPost newsPost;
 
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Transient
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String body;
 
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public NewsPost getNewsPost() { return newsPost; }
    public void setNewsPost(NewsPost newsPost) { this.newsPost = newsPost; }
    public Comment getParent() { return parent; }
    public void setParent(Comment parent) { this.parent = parent; }
    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
}
 