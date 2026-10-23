package com.tylerandrews.lcg.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Builds MockMvc from the full application context. Views are really
// rendered, so a mistake in a Thymeleaf template fails these tests too.
@SpringBootTest
class PageControllerTest {

    @Autowired
    private WebApplicationContext context;

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
}