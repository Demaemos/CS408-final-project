package com.tylerandrews.lcg;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.GameRepository;
import com.tylerandrews.lcg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
class AdminControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    @Autowired
    private GameRepository gameRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void adminRoutesRedirectGuestsAndRejectRegularUsers() throws Exception {
        User user = userService.register("adminrouteuser", "adminrouteuser@example.com", "regular-password");

        mockMvc.perform(get("/admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"));
        mockMvc.perform(get("/admin").session(sessionFor(user)))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/games").session(sessionFor(user)))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanOpenDashboardAndCreateEditAndDeleteGames() throws Exception {
        User admin = userService.createAdmin("admincruduser", "admincruduser@example.com", "admin-password");
        MockHttpSession session = sessionFor(admin);

        mockMvc.perform(get("/admin").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/games/new").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/game-form"));

        mockMvc.perform(post("/admin/games").session(session)
                .param("title", "Admin Managed Game")
                .param("slug", "admin-managed-game")
                .param("releaseStatus", "ANNOUNCED"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));

        Game created = gameRepository.findBySlug("admin-managed-game").orElseThrow();
        assertThat(created.getTitle()).isEqualTo("Admin Managed Game");

        mockMvc.perform(get("/admin/games/" + created.getId() + "/edit").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/game-form"));
        mockMvc.perform(post("/admin/games/" + created.getId()).session(session)
                .param("title", "Updated Admin Game")
                .param("slug", "admin-managed-game")
                .param("releaseStatus", "RELEASED"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
        assertThat(gameRepository.findById(created.getId()).orElseThrow().getTitle())
            .isEqualTo("Updated Admin Game");

        mockMvc.perform(post("/admin/games/" + created.getId() + "/delete").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
        assertThat(gameRepository.findById(created.getId())).isEmpty();
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.ROLE, user.getRole().name());
        return session;
    }
}
