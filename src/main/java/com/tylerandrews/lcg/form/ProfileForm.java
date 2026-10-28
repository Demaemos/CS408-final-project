package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileForm {

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters.")
    @Pattern(regexp = "[A-Za-z0-9_.-]*", message = "Use only letters, numbers, dots, dashes, and underscores.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 254, message = "Email is too long.")
    private String email;

    public ProfileForm() {
    }

    public ProfileForm(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}