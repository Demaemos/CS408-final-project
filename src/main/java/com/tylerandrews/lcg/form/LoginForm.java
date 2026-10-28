package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "Enter your username or email.")
    private String usernameOrEmail;

    @NotBlank(message = "Enter your password.")
    private String password;

    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}