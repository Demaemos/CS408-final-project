package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Test
    void seededGamesAreLoaded() {
        assertThat(gameRepository.count()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void findBySlugReturnsTheMatchingGame() {
        assertThat(gameRepository.findBySlug("untitled-project-one"))
            .isPresent()
            .get()
            .extracting(Game::getTitle)
            .isEqualTo("Untitled Project One");
    }

    @Test
    void findBySlugReturnsEmptyForUnknownSlug() {
        assertThat(gameRepository.findBySlug("no-such-game")).isEmpty();
    }

    @Test
    void existsBySlugReflectsWhatIsStored() {
        assertThat(gameRepository.existsBySlug("untitled-project-one")).isTrue();
        assertThat(gameRepository.existsBySlug("no-such-game")).isFalse();
    }
}