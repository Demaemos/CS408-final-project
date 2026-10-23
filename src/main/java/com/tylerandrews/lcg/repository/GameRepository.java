package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
    boolean existsBySlug(String slug);
    Optional<Game> findBySlug(String slug);
}