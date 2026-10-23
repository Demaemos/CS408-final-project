package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.Genre;
import com.tylerandrews.lcg.model.Platform;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.repository.GenreRepository;
import com.tylerandrews.lcg.repository.PlatformRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Each test saves its own uniquely named games, so the assertions don't
// depend on the seeded data. @Transactional rolls everything back afterward.
@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private PlatformRepository platformRepository;

    private Game newGame(String title, Game.ReleaseStatus status) {
        Game game = new Game();
        game.setTitle(title);
        game.setSlug(title.toLowerCase().replace(' ', '-'));
        game.setTagline("Test tagline");
        game.setDescription("Test description");
        game.setReleaseStatus(status);
        return game;
    }

    @Test
    void noFiltersReturnsGames() {
        assertThat(gameService.search(null, null, null, null)).isNotEmpty();
    }

    @Test
    void blankKeywordIsTreatedAsNoFilter() {
        int all = gameService.search(null, null, null, null).size();
        assertThat(gameService.search("   ", null, null, null)).hasSize(all);
    }

    @Test
    void keywordMatchesTitleIgnoringCase() {
        gameRepository.saveAndFlush(newGame("Zebra Quest Alpha", Game.ReleaseStatus.RELEASED));

        List<Game> results = gameService.search("zEbRa qUeSt", null, null, null);

        assertThat(results).extracting(Game::getTitle).containsExactly("Zebra Quest Alpha");
    }

    @Test
    void keywordWithNoMatchReturnsNothing() {
        assertThat(gameService.search("zzz-no-game-has-this-zzz", null, null, null)).isEmpty();
    }

    @Test
    void statusFilterNarrowsResults() {
        gameRepository.saveAndFlush(newGame("Zebra One", Game.ReleaseStatus.RELEASED));
        gameRepository.saveAndFlush(newGame("Zebra Two", Game.ReleaseStatus.IN_DEV));

        List<Game> results = gameService.search("zebra", null, null, Game.ReleaseStatus.RELEASED);

        assertThat(results).extracting(Game::getTitle).containsExactly("Zebra One");
    }

    @Test
    void genreFilterOnlyReturnsGamesWithThatGenre() {
        Genre genre = genreRepository.saveAndFlush(new Genre("Test Genre Xyz"));
        Game tagged = newGame("Genre Test Game", Game.ReleaseStatus.ANNOUNCED);
        tagged.getGenres().add(genre);
        gameRepository.saveAndFlush(tagged);
        gameRepository.saveAndFlush(newGame("Untagged Test Game", Game.ReleaseStatus.ANNOUNCED));

        List<Game> results = gameService.search(null, genre.getId(), null, null);

        assertThat(results).extracting(Game::getTitle).containsExactly("Genre Test Game");
    }

    @Test
    void platformFilterOnlyReturnsGamesOnThatPlatform() {
        Platform platform = platformRepository.saveAndFlush(new Platform("Test Platform Xyz"));
        Game onPlatform = newGame("Platform Test Game", Game.ReleaseStatus.ANNOUNCED);
        onPlatform.getPlatforms().add(platform);
        gameRepository.saveAndFlush(onPlatform);
        gameRepository.saveAndFlush(newGame("Elsewhere Test Game", Game.ReleaseStatus.ANNOUNCED));

        List<Game> results = gameService.search(null, null, platform.getId(), null);

        assertThat(results).extracting(Game::getTitle).containsExactly("Platform Test Game");
    }

    @Test
    void filtersCombineWithAnd() {
        Genre genre = genreRepository.saveAndFlush(new Genre("Combo Genre Xyz"));

        Game match = newGame("Combo Match", Game.ReleaseStatus.IN_DEV);
        match.getGenres().add(genre);
        gameRepository.saveAndFlush(match);

        Game wrongStatus = newGame("Combo Wrong Status", Game.ReleaseStatus.RELEASED);
        wrongStatus.getGenres().add(genre);
        gameRepository.saveAndFlush(wrongStatus);

        List<Game> results = gameService.search("combo", genre.getId(), null, Game.ReleaseStatus.IN_DEV);

        assertThat(results).extracting(Game::getTitle).containsExactly("Combo Match");
    }
}