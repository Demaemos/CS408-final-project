package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.form.AdminGameForm;
import com.tylerandrews.lcg.form.AdminJobForm;
import com.tylerandrews.lcg.form.AdminNewsForm;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.JobApplication;
import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.repository.JobApplicationRepository;
import com.tylerandrews.lcg.repository.JobPostingRepository;
import com.tylerandrews.lcg.repository.NewsPostRepository;
import com.tylerandrews.lcg.service.GameService;
import com.tylerandrews.lcg.service.JobApplicationService;
import com.tylerandrews.lcg.service.JobPostingService;
import com.tylerandrews.lcg.service.NewsPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final GameService gameService;
    private final GameRepository gameRepository;
    private final NewsPostService newsPostService;
    private final NewsPostRepository newsPostRepository;
    private final JobPostingService jobPostingService;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationService jobApplicationService;
    private final JobApplicationRepository jobApplicationRepository;

    public AdminController(GameService gameService,
                          GameRepository gameRepository,
                          NewsPostService newsPostService,
                          NewsPostRepository newsPostRepository,
                          JobPostingService jobPostingService,
                          JobPostingRepository jobPostingRepository,
                          JobApplicationService jobApplicationService,
                          JobApplicationRepository jobApplicationRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.newsPostService = newsPostService;
        this.newsPostRepository = newsPostRepository;
        this.jobPostingService = jobPostingService;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationService = jobApplicationService;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("games", gameService.allGames());
        model.addAttribute("posts", newsPostService.latest(5));
        var jobs = jobPostingService.allJobs();
        model.addAttribute("jobs", jobs);
        model.addAttribute("applicationCounts", jobs.stream().collect(Collectors.toMap(
            JobPosting::getId, jobApplicationService::countByJob)));
        model.addAttribute("applications", jobApplicationService.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/admin/games/new")
    public String newGame(Model model) {
        model.addAttribute("gameForm", new AdminGameForm());
        prepareForm(model);
        return "admin/game-form";
    }

    @GetMapping("/admin/jobs/new")
    public String newJob(Model model) {
        model.addAttribute("jobForm", new AdminJobForm());
        return "admin/job-form";
    }

    @GetMapping("/admin/news/new")
    public String newNews(Model model) {
        model.addAttribute("newsForm", new AdminNewsForm());
        return "admin/news-form";
    }

    @PostMapping("/admin/jobs")
    public String createJob(@Valid @ModelAttribute("jobForm") AdminJobForm form,
                            BindingResult result,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/job-form";
        }

        JobPosting job = new JobPosting();
        copyJobForm(form, job);
        jobPostingRepository.save(job);
        redirect.addFlashAttribute("message", "Job posting created.");
        return "redirect:/admin";
    }

    @GetMapping("/admin/jobs/{id}/edit")
    public String editJob(@PathVariable Long id, Model model) {
        JobPosting job = findJob(id);
        model.addAttribute("jobId", id);
        model.addAttribute("jobForm", toJobForm(job));
        return "admin/job-form";
    }

    @PostMapping("/admin/jobs/{id}")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobForm") AdminJobForm form,
                            BindingResult result,
                            RedirectAttributes redirect) {
        JobPosting job = findJob(id);
        if (result.hasErrors()) {
            return "admin/job-form";
        }
        copyJobForm(form, job);
        jobPostingRepository.save(job);
        redirect.addFlashAttribute("message", "Job posting updated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, RedirectAttributes redirect) {
        jobPostingService.delete(findJob(id));
        redirect.addFlashAttribute("message", "Job posting deleted.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/applications/{id}/status")
    public String updateApplicationStatus(@PathVariable Long id,
                                         @RequestParam("status") String status,
                                         RedirectAttributes redirect) {
        JobApplication application = jobApplicationService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        try {
            JobApplication.Status applicationStatus = JobApplication.Status.valueOf(status.trim().toUpperCase());
            jobApplicationService.updateStatus(application, applicationStatus);
            redirect.addFlashAttribute("message", "Application status updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("message", "Invalid status value.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/applications/{id}/delete")
    public String deleteApplication(@PathVariable Long id, RedirectAttributes redirect) {
        JobApplication application = jobApplicationService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (application.getStatus() != JobApplication.Status.REJECTED) {
            redirect.addFlashAttribute("message", "Only rejected applications can be removed.");
            return "redirect:/admin";
        }
        jobApplicationService.withdraw(application);
        redirect.addFlashAttribute("message", "Rejected application removed.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/news")
    public String createNews(@Valid @ModelAttribute("newsForm") AdminNewsForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        validateNewsSlug(form, null, result);
        if (result.hasErrors()) {
            return "admin/news-form";
        }

        NewsPost post = new NewsPost();
        copyNewsForm(form, post);
        newsPostRepository.save(post);
        redirect.addFlashAttribute("message", "News post created.");
        return "redirect:/admin";
    }

    @GetMapping("/admin/news/{id}/edit")
    public String editNews(@PathVariable Long id, Model model) {
        NewsPost post = findNews(id);
        model.addAttribute("newsId", id);
        model.addAttribute("newsForm", toNewsForm(post));
        return "admin/news-form";
    }

    @PostMapping("/admin/news/{id}")
    public String updateNews(@PathVariable Long id,
                             @Valid @ModelAttribute("newsForm") AdminNewsForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        NewsPost post = findNews(id);
        validateNewsSlug(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("newsId", id);
            return "admin/news-form";
        }

        copyNewsForm(form, post);
        newsPostRepository.save(post);
        redirect.addFlashAttribute("message", "News post updated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/news/{id}/delete")
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirect) {
        newsPostRepository.delete(findNews(id));
        redirect.addFlashAttribute("message", "News post deleted.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/games")
    public String createGame(@Valid @ModelAttribute("gameForm") AdminGameForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        validateSlug(form, null, result);
        if (result.hasErrors()) {
            prepareForm(model);
            return "admin/game-form";
        }

        Game game = new Game();
        copyForm(form, game);
        gameService.save(game, form.getGenreIds(), form.getPlatformIds());
        redirect.addFlashAttribute("message", "Game created.");
        return "redirect:/admin";
    }

    @GetMapping("/admin/games/{id}/edit")
    public String editGame(@PathVariable Long id, Model model) {
        Game game = findGame(id);
        model.addAttribute("gameId", id);
        model.addAttribute("gameForm", toForm(game));
        prepareForm(model);
        return "admin/game-form";
    }

    @PostMapping("/admin/games/{id}")
    public String updateGame(@PathVariable Long id,
                             @Valid @ModelAttribute("gameForm") AdminGameForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        Game game = findGame(id);
        validateSlug(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("gameId", id);
            prepareForm(model);
            return "admin/game-form";
        }

        copyForm(form, game);
        gameService.save(game, form.getGenreIds(), form.getPlatformIds());
        redirect.addFlashAttribute("message", "Game updated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/games/{id}/delete")
    public String deleteGame(@PathVariable Long id, RedirectAttributes redirect) {
        gameService.delete(findGame(id));
        redirect.addFlashAttribute("message", "Game deleted.");
        return "redirect:/admin";
    }

    private NewsPost findNews(Long id) {
        return newsPostService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News post not found"));
    }

    private Game findGame(Long id) {
        return gameService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private JobPosting findJob(Long id) {
        return jobPostingService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job posting not found"));
    }

    private void validateSlug(AdminGameForm form, Long currentId, BindingResult result) {
        gameRepository.findBySlug(form.getSlug())
            .filter(existing -> !Objects.equals(existing.getId(), currentId))
            .ifPresent(existing -> result.rejectValue("slug", "taken", "That slug is already in use."));
    }

    private void validateNewsSlug(AdminNewsForm form, Long currentId, BindingResult result) {
        newsPostRepository.findBySlug(form.getSlug().trim())
            .filter(existing -> !Objects.equals(existing.getId(), currentId))
            .ifPresent(existing -> result.rejectValue("slug", "taken", "That slug is already in use."));
    }

    private void prepareForm(Model model) {
        model.addAttribute("genres", gameService.genres());
        model.addAttribute("platforms", gameService.platforms());
        model.addAttribute("statuses", Game.ReleaseStatus.values());
    }

    private void copyForm(AdminGameForm form, Game game) {
        game.setTitle(form.getTitle().trim());
        game.setSlug(form.getSlug().trim());
        game.setTagline(form.getTagline());
        game.setDescription(form.getDescription());
        game.setReleaseStatus(form.getReleaseStatus());
        game.setReleaseDate(form.getReleaseDate());
        game.setEsrbRating(form.getEsrbRating());
        game.setCoverImage(form.getCoverImage());
        game.setTrailerUrl(form.getTrailerUrl());
    }

    private AdminGameForm toForm(Game game) {
        AdminGameForm form = new AdminGameForm();
        form.setTitle(game.getTitle());
        form.setSlug(game.getSlug());
        form.setTagline(game.getTagline());
        form.setDescription(game.getDescription());
        form.setReleaseStatus(game.getReleaseStatus());
        form.setReleaseDate(game.getReleaseDate());
        form.setEsrbRating(game.getEsrbRating());
        form.setCoverImage(game.getCoverImage());
        form.setTrailerUrl(game.getTrailerUrl());
        form.setGenreIds(game.getGenres().stream().map(genre -> genre.getId())
            .collect(java.util.stream.Collectors.toSet()));
        form.setPlatformIds(game.getPlatforms().stream().map(platform -> platform.getId())
            .collect(java.util.stream.Collectors.toSet()));
        return form;
    }

    private void copyNewsForm(AdminNewsForm form, NewsPost post) {
        post.setTitle(form.getTitle().trim());
        post.setSlug(form.getSlug().trim());
        post.setBody(form.getBody());
        post.setTags(normalizeTags(form.getTags()));
        post.setImage(form.getImage());
    }

    private void copyJobForm(AdminJobForm form, JobPosting job) {
        job.setTitle(form.getTitle() == null ? null : form.getTitle().trim());
        job.setDepartment(form.getDepartment() == null ? null : form.getDepartment().trim());
        job.setLocation(form.getLocation() == null ? null : form.getLocation().trim());
        job.setDescription(form.getDescription() == null ? null : form.getDescription().trim());
        job.setRequirements(form.getRequirements() == null ? null : form.getRequirements().trim());
        job.setOpen(form.isOpen());
    }

    private AdminJobForm toJobForm(JobPosting job) {
        AdminJobForm form = new AdminJobForm();
        form.setTitle(job.getTitle());
        form.setDepartment(job.getDepartment());
        form.setLocation(job.getLocation());
        form.setDescription(job.getDescription());
        form.setRequirements(job.getRequirements());
        form.setOpen(job.isOpen());
        return form;
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        String normalized = Arrays.stream(tags.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.joining(","));
        return normalized.isEmpty() ? null : normalized;
    }

    private AdminNewsForm toNewsForm(NewsPost post) {
        AdminNewsForm form = new AdminNewsForm();
        form.setTitle(post.getTitle());
        form.setSlug(post.getSlug());
        form.setBody(post.getBody());
        form.setTags(post.getTags());
        form.setImage(post.getImage());
        return form;
    }
}
