package com.tylerandrews.lcg.repository;
 
import com.tylerandrews.lcg.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface GenreRepository extends JpaRepository<Genre, Long> {
}
 