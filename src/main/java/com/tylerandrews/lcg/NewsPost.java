package com.tylerandrews.lcg;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "news_posts")
public class NewsPost {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String title;
 
    @Column(nullable = false, unique = true)
    private String slug;
 
    @Column(length = 8000)
    private String body;
 
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
 
    private String tags;
    private LocalDateTime publishedAt = LocalDateTime.now();
    private String image;
 
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
 