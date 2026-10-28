package com.tylerandrews.lcg.form;

import com.tylerandrews.lcg.model.Game;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class AdminGameForm {

    @NotBlank(message = "Title is required.")
    @Size(max = 255, message = "Title must be 255 characters or fewer.")
    private String title;

    @NotBlank(message = "Slug is required.")
    @Size(max = 255, message = "Slug must be 255 characters or fewer.")
    @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "Use lowercase letters, numbers, and single dashes.")
    private String slug;

    @Size(max = 255, message = "Tagline must be 255 characters or fewer.")
    private String tagline;

    @Size(max = 4000, message = "Description must be 4000 characters or fewer.")
    private String description;

    @NotNull(message = "Choose a release status.")
    private Game.ReleaseStatus releaseStatus = Game.ReleaseStatus.ANNOUNCED;

    private LocalDate releaseDate;
    private String esrbRating;
    private String coverImage;
    private String trailerUrl;
    private Set<Long> genreIds = new HashSet<>();
    private Set<Long> platformIds = new HashSet<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Game.ReleaseStatus getReleaseStatus() { return releaseStatus; }
    public void setReleaseStatus(Game.ReleaseStatus releaseStatus) { this.releaseStatus = releaseStatus; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getEsrbRating() { return esrbRating; }
    public void setEsrbRating(String esrbRating) { this.esrbRating = esrbRating; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Set<Long> getGenreIds() { return genreIds; }
    public void setGenreIds(Set<Long> genreIds) { this.genreIds = genreIds; }
    public Set<Long> getPlatformIds() { return platformIds; }
    public void setPlatformIds(Set<Long> platformIds) { this.platformIds = platformIds; }
}
