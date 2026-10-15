package com.tylerandrews.lcg;
 
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
 
@Entity
@Table(name = "genres")
public class Genre {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String name;
 
    @ManyToMany(mappedBy = "genres")
    private Set<Game> games = new HashSet<>();
 
    public Genre() {}
    public Genre(String name) { this.name = name; }
 
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Game> getGames() { return games; }
}
 