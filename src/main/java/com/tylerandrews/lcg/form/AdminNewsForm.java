package com.tylerandrews.lcg.form;

import jakarta.validation.constraints.NotBlank;

public class AdminNewsForm {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Slug is required.")
    private String slug;

    @NotBlank(message = "Body is required.")
    private String body;

    private String tags;
    private String image;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
