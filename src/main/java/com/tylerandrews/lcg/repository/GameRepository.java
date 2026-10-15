package com.tylerandrews.lcg.repository;
 
import com.tylerandrews.lcg.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsBySlug(String slug);
}
 