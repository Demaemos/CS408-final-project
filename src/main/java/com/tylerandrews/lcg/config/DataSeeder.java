package com.tylerandrews.lcg.config;

import com.tylerandrews.lcg.model.*;
import com.tylerandrews.lcg.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
// Seeds starter data through JPA repositories after the full application
// context (and schema) is guaranteed to exist. This avoids the data.sql /
// defer-datasource-initialization ordering problem entirely, since a
// CommandLineRunner always runs after the context has fully started.
//
// Game titles are placeholders: the proposal describes the studio's games
// generically as "Coming Soon" with no names/lore committed yet.
@Component
public class DataSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JobPostingRepository jobPostingRepository;
 
    public DataSeeder(GenreRepository genreRepository,
                       PlatformRepository platformRepository,
                       GameRepository gameRepository,
                       TeamMemberRepository teamMemberRepository,
                       JobPostingRepository jobPostingRepository) {
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.jobPostingRepository = jobPostingRepository;
    }
 
    @Override
    public void run(String... args) {
        if (genreRepository.count() == 0) {
            genreRepository.saveAll(List.of(
                new Genre("Action"), new Genre("Adventure"),
                new Genre("Puzzle"), new Genre("Platformer")));
        }
 
        if (platformRepository.count() == 0) {
            platformRepository.saveAll(List.of(
                new Platform("PC"), new Platform("PlayStation"),
                new Platform("Xbox"), new Platform("Switch")));
        }
 
        if (!gameRepository.existsBySlug("untitled-project-one")) {
            Game g1 = new Game();
            g1.setTitle("Untitled Project One");
            g1.setSlug("untitled-project-one");
            g1.setTagline("Coming soon.");
            g1.setDescription("Details to be revealed as development progresses.");
            g1.setReleaseStatus(Game.ReleaseStatus.ANNOUNCED);
            g1.setEsrbRating("E10+");
            gameRepository.save(g1);
        }
 
        if (!gameRepository.existsBySlug("untitled-project-two")) {
            Game g2 = new Game();
            g2.setTitle("Untitled Project Two");
            g2.setSlug("untitled-project-two");
            g2.setTagline("Coming soon.");
            g2.setDescription("Details to be revealed as development progresses.");
            g2.setReleaseStatus(Game.ReleaseStatus.ANNOUNCED);
            g2.setEsrbRating("E");
            gameRepository.save(g2);
        }
 
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
    }
}
 