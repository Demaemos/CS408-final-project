package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportTicketForm {

    @NotBlank(message = "Enter your name.")
    @Size(max = 120, message = "Name must be 120 characters or fewer.")
    private String name;

    @NotBlank(message = "Enter your email address.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 254, message = "Email must be 254 characters or fewer.")
    private String email;

    @NotBlank(message = "Choose a topic.")
    @Size(max = 120, message = "Topic must be 120 characters or fewer.")
    private String topic;

    private Long gameId;

    @NotBlank(message = "Describe how we can help.")
    @Size(max = 4000, message = "Message must be 4000 characters or fewer.")
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
