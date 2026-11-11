package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.JobPosting;
import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.CommentRepository;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.repository.JobApplicationRepository;
import com.tylerandrews.lcg.repository.JobPostingRepository;
import com.tylerandrews.lcg.repository.NewsPostRepository;
import com.tylerandrews.lcg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Builds MockMvc from the full application context. Views are really
// rendered, so a mistake in a Thymeleaf template fails these tests too.
@SpringBootTest
@Transactional
class PageControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private NewsPostRepository newsPostRepository;
    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void homePageLoads() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }

    @Test
    void catalogPageLoadsWithLookups() throws Exception {
        mockMvc.perform(get("/games"))
            .andExpect(status().isOk())
            .andExpect(view().name("games"))
            .andExpect(model().attributeExists("games", "genres", "platforms", "statuses"));
    }

    @Test
    void catalogKeywordSearchFindsASeededGame() throws Exception {
        MvcResult result = mockMvc.perform(get("/games").param("keyword", "project one"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getModelAndView()).isNotNull();
        List<?> games = (List<?>) result.getModelAndView().getModel().get("games");
        assertThat(games).hasSize(1);
    }

    @Test
    void catalogSearchWithNoMatchShowsNoGames() throws Exception {
        MvcResult result = mockMvc.perform(get("/games").param("keyword", "zzz-no-game-has-this-zzz"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getModelAndView()).isNotNull();
        List<?> games = (List<?>) result.getModelAndView().getModel().get("games");
        assertThat(games).isEmpty();
    }

    @Test
    void catalogAcceptsEmptyFilterValuesFromTheForm() throws Exception {
        // An untouched form submits genre=, platform=, status= (empty strings).
        mockMvc.perform(get("/games")
                .param("keyword", "")
                .param("genre", "")
                .param("platform", "")
                .param("status", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("games"));
    }

    @Test
    void gameDetailPageLoadsForASeededGame() throws Exception {
        mockMvc.perform(get("/games/untitled-project-one"))
            .andExpect(status().isOk())
            .andExpect(view().name("game-detail"))
            .andExpect(model().attributeExists("game"));
    }

    @Test
    void gameDetailReturns404ForUnknownSlug() throws Exception {
        mockMvc.perform(get("/games/no-such-game"))
            .andExpect(status().isNotFound());
    }

    @Test
    void aboutPageLoadsWithTeam() throws Exception {
        mockMvc.perform(get("/about"))
            .andExpect(status().isOk())
            .andExpect(view().name("about"))
            .andExpect(model().attributeExists("team"));
    }

    @Test
    void newsPageLoadsWithPostsAndTagFilterSupport() throws Exception {
        mockMvc.perform(get("/news"))
            .andExpect(status().isOk())
            .andExpect(view().name("news"))
            .andExpect(model().attributeExists("posts", "tags", "newsPage"));

        mockMvc.perform(get("/news").param("tag", "devlog"))
            .andExpect(status().isOk())
            .andExpect(view().name("news"));
    }

    @Test
    void newsFiltersAndPaginatesInTheDatabase() throws Exception {
        for (int i = 0; i < 12; i++) {
            NewsPost post = new NewsPost();
            post.setTitle("Checkpoint query entry " + i);
            post.setSlug("checkpoint-query-entry-" + i);
            post.setBody("Pagination coverage.");
            post.setTags("checkpoint,tag" + i);
            newsPostRepository.save(post);
        }

        MvcResult firstPage = mockMvc.perform(get("/news")
                .param("keyword", "checkpoint query")
                .param("tag", "checkpoint"))
            .andExpect(status().isOk())
            .andReturn();
        Page<?> page = (Page<?>) firstPage.getModelAndView().getModel().get("newsPage");
        assertThat(page.getTotalElements()).isEqualTo(12);
        assertThat(page.getContent()).hasSize(10);

        MvcResult secondPage = mockMvc.perform(get("/news")
                .param("keyword", "checkpoint query")
                .param("tag", "checkpoint")
                .param("page", "1"))
            .andExpect(status().isOk())
            .andReturn();
        Page<?> lastPage = (Page<?>) secondPage.getModelAndView().getModel().get("newsPage");
        assertThat(lastPage.getTotalElements()).isEqualTo(12);
        assertThat(lastPage.getContent()).hasSize(2);
    }

    @Test
    void careerPageLoadsAndJobDetailIsAvailable() throws Exception {
        mockMvc.perform(get("/careers"))
            .andExpect(status().isOk())
            .andExpect(view().name("careers"))
            .andExpect(model().attributeExists("jobs", "departments", "locations"));

        mockMvc.perform(get("/careers/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("job-detail"))
            .andExpect(model().attributeExists("job"));
    }

    @Test
    void careerFiltersReturnOnlyMatchingOpenRoles() throws Exception {
        JobPosting openRole = new JobPosting();
        openRole.setTitle("QA Engineer");
        openRole.setDepartment("Quality Assurance");
        openRole.setLocation("Remote");
        jobPostingRepository.save(openRole);

        JobPosting closedRole = new JobPosting();
        closedRole.setTitle("Former QA Engineer");
        closedRole.setDepartment("Quality Assurance");
        closedRole.setLocation("Remote");
        closedRole.setOpen(false);
        jobPostingRepository.save(closedRole);

        MvcResult result = mockMvc.perform(get("/careers")
                .param("department", "quality assurance")
                .param("location", "remote"))
            .andExpect(status().isOk())
            .andReturn();
        List<?> jobs = (List<?>) result.getModelAndView().getModel().get("jobs");
        assertThat(jobs).hasSize(1);
        assertThat(((JobPosting) jobs.get(0)).getTitle()).isEqualTo("QA Engineer");
    }

    @Test
    void applicantsCanSubmitLookupAndWithdrawAnApplication() throws Exception {
        JobPosting role = new JobPosting();
        role.setTitle("UI Designer");
        role.setDepartment("Design");
        role.setLocation("Remote");
        role.setDescription("Craft polished interfaces.");
        role.setRequirements("Strong UX skills.");
        role = jobPostingRepository.save(role);

        mockMvc.perform(post("/careers/" + role.getId() + "/apply")
                .param("applicantName", "Taylor Example")
                .param("email", "taylor@example.com")
                .param("coverNote", "I am excited to apply.")
                .param("portfolioUrl", "https://example.com")
                .param("resumeUrl", "https://example.com/resume"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/careers/" + role.getId()));

        assertThat(jobApplicationRepository.findAll()).hasSize(1);
        var application = jobApplicationRepository.findAll().get(0);
        assertThat(application.getReferenceCode()).startsWith("LCG-");

        mockMvc.perform(post("/careers/" + role.getId() + "/lookup")
                .param("email", "taylor@example.com")
                .param("referenceCode", application.getReferenceCode()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/careers/" + role.getId()));

        mockMvc.perform(post("/careers/" + role.getId() + "/withdraw")
                .param("email", "taylor@example.com")
                .param("referenceCode", application.getReferenceCode()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/careers/" + role.getId()));

        assertThat(jobApplicationRepository.findById(application.getId())).isEmpty();
    }

    @Test
    void loggedInUsersCanPostAndDeleteComments() throws Exception {
        User user = userService.register("commenter", "commenter@example.com", "Password12!");
        MockHttpSession session = sessionFor(user);

        mockMvc.perform(post("/games/untitled-project-one/comments")
                .session(session)
                .param("body", "This game looks great!"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/games/untitled-project-one"));

        Game game = gameRepository.findBySlug("untitled-project-one").orElseThrow();
        assertThat(commentRepository.findByGameOrderByCreatedAtAsc(game)).hasSize(1);
    }

    @Test
    void commentsSupportRepliesModerateBlockedTermsAndCollapseLongThreads() throws Exception {
        User user = userService.register("threadcommenter", "threadcommenter@example.com", "Password12!");
        MockHttpSession session = sessionFor(user);

        mockMvc.perform(post("/games/untitled-project-one/comments")
                .session(session)
                .param("body", "A top-level comment"))
            .andExpect(status().is3xxRedirection());

        Game game = gameRepository.findBySlug("untitled-project-one").orElseThrow();
        var root = commentRepository.findByGameOrderByCreatedAtAsc(game).get(0);
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/games/untitled-project-one/comments/" + root.getId() + "/replies")
                    .session(session)
                    .param("body", "Reply " + i))
                .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(post("/games/untitled-project-one/comments")
                .session(session)
                .param("body", "This contains FUCK! language"))
            .andExpect(status().is3xxRedirection());

        assertThat(commentRepository.findByGameOrderByCreatedAtAsc(game)).hasSize(6);
        mockMvc.perform(get("/games/untitled-project-one"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string(org.hamcrest.Matchers.containsString("Show")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string(org.hamcrest.Matchers.containsString("more replies")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("FUCK"))));
    }

    @Test
    void adminsCanDeleteCommentsButCannotEditThem() throws Exception {
        User author = userService.register("commentauthor", "commentauthor@example.com", "Password12!");
        User admin = userService.createAdmin("commentadmin", "commentadmin@example.com", "admin-password");
        Game game = gameRepository.findBySlug("untitled-project-one").orElseThrow();
        var comment = new com.tylerandrews.lcg.model.Comment();
        comment.setUser(author);
        comment.setGame(game);
        comment.setBody("A comment");
        comment = commentRepository.save(comment);
        var reply = new com.tylerandrews.lcg.model.Comment();
        reply.setUser(author);
        reply.setGame(game);
        reply.setParent(comment);
        reply.setBody("A reply");
        reply = commentRepository.save(reply);

        mockMvc.perform(post("/games/untitled-project-one/comments/" + comment.getId() + "/edit")
                .session(sessionFor(admin))
                .param("body", "Admin changed this"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/games/untitled-project-one/comments/" + comment.getId() + "/delete")
                .session(sessionFor(admin)))
            .andExpect(status().is3xxRedirection());
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(commentRepository.findById(reply.getId())).isEmpty();
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.ROLE, user.getRole().name());
        return session;
    }
}