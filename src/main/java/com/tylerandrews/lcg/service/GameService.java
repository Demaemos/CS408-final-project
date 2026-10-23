package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.Genre;
import com.tylerandrews.lcg.model.Platform;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.repository.GenreRepository;
import com.tylerandrews.lcg.repository.PlatformRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    public GameService(GameRepository gameRepository,
                       GenreRepository genreRepository,
                       PlatformRepository platformRepository) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
    }

    /**
     * Searches games. Every argument is optional: null (or a blank keyword)
     * means "don't filter on this". Filters combine with AND, and the
     * keyword matches title, tagline, or description, ignoring case.
     * Only the matching rows are loaded from the database.
     */
    public List<Game> search(String keyword, Long genreId, Long platformId, Game.ReleaseStatus status) {
        Specification<Game> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("tagline")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
            }
            if (genreId != null) {
                predicates.add(cb.equal(root.join("genres").get("id"), genreId));
            }
            if (platformId != null) {
                predicates.add(cb.equal(root.join("platforms").get("id"), platformId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("releaseStatus"), status));
            }

            if (query != null) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return gameRepository.findAll(spec, Sort.by("title"));
    }

    public Optional<Game> findBySlug(String slug) {
        return gameRepository.findBySlug(slug);
    }

    public List<Genre> genres() {
        return genreRepository.findAll(Sort.by("name"));
    }

    public List<Platform> platforms() {
        return platformRepository.findAll(Sort.by("name"));
    }
}