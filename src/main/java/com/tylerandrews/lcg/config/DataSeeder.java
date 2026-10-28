package com.tylerandrews.lcg.config;

import com.tylerandrews.lcg.model.*;
import com.tylerandrews.lcg.repository.*;
import com.tylerandrews.lcg.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Seeds starter data through JPA repositories after the full application
// context (and schema) exists. Safe to run repeatedly: every insert checks
// for an existing row first.
//
// Game titles are placeholders: the proposal describes the studio's games
// generically as "Coming Soon" with no names/lore committed yet.
//
// An admin account is created only when lcg.admin.password is set:
//  - dev profile (./start.sh): application-dev.properties sets a throwaway password
//  - production: set LCG_ADMIN_PASSWORD in /etc/lcg/app.env on the server
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String PLACEHOLDER_DESCRIPTION =
        "Details to be revealed as development progresses.";

    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(GenreRepository genreRepository,
                      PlatformRepository platformRepository,
                      GameRepository gameRepository,
                      TeamMemberRepository teamMemberRepository,
                      JobPostingRepository jobPostingRepository,
                      UserService userService,
                      @Value("${lcg.admin.username:admin}") String adminUsername,
                      @Value("${lcg.admin.password:}") String adminPassword) {
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.userService = userService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        // Lookup values first, so they exist even if no game uses them yet.
        for (String name : List.of("Action", "Adventure", "Puzzle", "Platformer")) {
            genre(name);
        }
        for (String name : List.of("PC", "PlayStation", "Xbox", "Switch")) {
            platform(name);
        }

        seedGame("Untitled Project One", "untitled-project-one", "Coming soon.",
            Game.ReleaseStatus.ANNOUNCED, "E10+",
            List.of("Action", "Adventure"), List.of("PC", "PlayStation", "Xbox"));

        seedGame("Untitled Project Two", "untitled-project-two", "Coming soon.",
            Game.ReleaseStatus.IN_DEV, "E",
            List.of("Puzzle"), List.of("PC", "Switch"));

        seedGame("Untitled Project Three", "untitled-project-three", "Coming soon.",
            Game.ReleaseStatus.ANNOUNCED, "T",
            List.of("Action", "Platformer"), List.of("PC", "Xbox", "Switch"));

        seedGame("Untitled Project Four", "untitled-project-four", "Coming soon.",
            Game.ReleaseStatus.IN_DEV, "E10+",
            List.of("Adventure", "Puzzle"), List.of("PC", "PlayStation"));

        if (teamMemberRepository.count() == 0) {
            TeamMember t1 = new TeamMember();
            t1.setName("Alex Rivera");
            t1.setRoleTitle("Founder & Creative Director");
            t1.setBio("Started Loose Cannon Games to make original, all-ages worlds.");
            t1.setSortOrder(1);

            TeamMember t2 = new TeamMember();
            t2.setName("Sam Park");
            t2.setRoleTitle("Lead Engineer");
            t2.setBio("Builds the systems that hold everything together.");
            t2.setSortOrder(2);

            teamMemberRepository.saveAll(List.of(t1, t2));
        }

        if (jobPostingRepository.count() == 0) {
            JobPosting job = new JobPosting();
            job.setTitle("Gameplay Engineer");
            job.setDepartment("Engineering");
            job.setLocation("Remote");
            job.setDescription("Build and ship gameplay systems.");
            job.setRequirements("2+ years of game development experience.");
            job.setOpen(true);
            jobPostingRepository.save(job);
        }

        if (!adminPassword.isBlank() && !userService.usernameTaken(adminUsername, null)) {
            userService.createAdmin(adminUsername, adminUsername + "@loosecannongames.example", adminPassword);
        }
    }

    private Genre genre(String name) {
        return genreRepository.findByName(name)
            .orElseGet(() -> genreRepository.save(new Genre(name)));
    }

    private Platform platform(String name) {
        return platformRepository.findByName(name)
            .orElseGet(() -> platformRepository.save(new Platform(name)));
    }

    private void seedGame(String title, String slug, String tagline,
                          Game.ReleaseStatus status, String esrbRating,
                          List<String> genreNames, List<String> platformNames) {
        if (gameRepository.existsBySlug(slug)) {
            return;
        }
        Game game = new Game();
        game.setTitle(title);
        game.setSlug(slug);
        game.setTagline(tagline);
        game.setDescription(PLACEHOLDER_DESCRIPTION);
        game.setReleaseStatus(status);
        game.setEsrbRating(esrbRating);
        for (String name : genreNames) {
            game.getGenres().add(genre(name));
        }
        for (String name : platformNames) {
            game.getPlatforms().add(platform(name));
        }
        gameRepository.save(game);
    }
}