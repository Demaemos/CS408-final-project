package com.tylerandrews.lcg;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
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
 
    @Column(nullable = false, length = 2000)
    private String body;
 
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;
 
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public NewsPost getNewsPost() { return newsPost; }
    public void setNewsPost(NewsPost newsPost) { this.newsPost = newsPost; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
}
 