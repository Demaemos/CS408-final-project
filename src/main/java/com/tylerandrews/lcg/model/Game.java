package com.tylerandrews.lcg.model;
 
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
 
@Entity
@Table(name = "games")
public class Game {
 
    public enum ReleaseStatus { 
        ANNOUNCED("Announced"), 
        IN_DEV("In Development"), 
        RELEASED("Released");

        private final String label;

        ReleaseStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String title;
 
    @Column(nullable = false, unique = true)
    private String slug;
 
    private String tagline;
 
    @Column(length = 4000)
    private String description;
 
    @Enumerated(EnumType.STRING)
    private ReleaseStatus releaseStatus = ReleaseStatus.ANNOUNCED;
 
    private LocalDate releaseDate;
    private String esrbRating;
    private String coverImage;
    private String trailerUrl;
 
    @ManyToMany
    @JoinTable(
        name = "game_genres",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();
 
    @ManyToMany
    @JoinTable(
        name = "game_platforms",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "platform_id"))
    private Set<Platform> platforms = new HashSet<>();
 
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReleaseStatus getReleaseStatus() { return releaseStatus; }
    public void setReleaseStatus(ReleaseStatus releaseStatus) { this.releaseStatus = releaseStatus; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getEsrbRating() { return esrbRating; }
    public void setEsrbRating(String esrbRating) { this.esrbRating = esrbRating; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Set<Genre> getGenres() { return genres; }
    public Set<Platform> getPlatforms() { return platforms; }
}
 