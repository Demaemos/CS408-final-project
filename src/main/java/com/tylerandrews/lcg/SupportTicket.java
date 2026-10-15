package com.tylerandrews.lcg;
 
import jakarta.persistence.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "support_tickets")
public class SupportTicket {
 
    public enum Status { OPEN, RESOLVED }
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(nullable = false)
    private String email;
 
    private String topic;
 
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
 
    @Column(nullable = false, length = 4000)
    private String message;
 
    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;
 
    @Column(length = 2000)
    private String adminNote;
 
    private LocalDateTime createdAt = LocalDateTime.now();
 
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
 