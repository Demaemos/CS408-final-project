package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentForm {

    @NotBlank(message = "Comment cannot be empty.")
    @Size(max = 2000, message = "Comment must be 2000 characters or fewer.")
    private String body;

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
