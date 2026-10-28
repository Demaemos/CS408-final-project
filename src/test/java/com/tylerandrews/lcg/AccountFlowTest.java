package com.tylerandrews.lcg;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.UserRepository;
import com.tylerandrews.lcg.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Each test runs inside a transaction that is rolled back afterward,
// so the accounts created here never leak between tests.
@SpringBootTest
@Transactional
class AccountFlowTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.ROLE, user.getRole().name());
        return session;
    }

    private boolean isLoggedIn(MvcResult result) {
        HttpSession session = result.getRequest().getSession(false);
        return session != null && session.getAttribute(SessionKeys.USER_ID) != null;
    }

    // ---- Account page ----

    @Test
    void accountPageShowsLoginAndRegisterWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/account"))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeExists("loginForm", "registerForm"))
            .andExpect(model().attributeDoesNotExist("user"));
    }

    @Test
    void accountPageShowsTheProfileWhenLoggedIn() throws Exception {
        User user = userService.register("profileview", "profileview@example.com", "some-password");

        mockMvc.perform(get("/account").session(sessionFor(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeExists("user", "profileForm"));
    }

    // ---- Register ----

    @Test
    void registerCreatesAHashedAccountAndLogsTheUserIn() throws Exception {
        MvcResult result = mockMvc.perform(post("/register")
                .param("username", "newplayer")
                .param("email", "NewPlayer@Example.com")
                .param("password", "correct-horse")
                .param("confirmPassword", "correct-horse"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"))
            .andReturn();

        User saved = userRepository.findByUsernameIgnoreCase("newplayer").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("newplayer@example.com");
        assertThat(saved.getPasswordHash()).isNotEqualTo("correct-horse").startsWith("$2");
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(SessionKeys.USER_ID)).isEqualTo(saved.getId());
        assertThat(session.getAttribute(SessionKeys.ROLE)).isEqualTo("USER");
    }

    @Test
    void registerRejectsADuplicateUsername() throws Exception {
        userService.register("dupname", "first@example.com", "some-password");

        mockMvc.perform(post("/register")
                .param("username", "DupName")
                .param("email", "second@example.com")
                .param("password", "correct-horse")
                .param("confirmPassword", "correct-horse"))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeHasFieldErrors("registerForm", "username"));
    }

    @Test
    void registerRejectsADuplicateEmail() throws Exception {
        userService.register("firstperson", "shared@example.com", "some-password");

        mockMvc.perform(post("/register")
                .param("username", "secondperson")
                .param("email", "SHARED@example.com")
                .param("password", "correct-horse")
                .param("confirmPassword", "correct-horse"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
    }

    @Test
    void registerRejectsMismatchedPasswords() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "mismatch")
                .param("email", "mismatch@example.com")
                .param("password", "correct-horse")
                .param("confirmPassword", "different-horse"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("registerForm", "confirmPassword"));
    }

    @Test
    void registerRejectsAShortPasswordAndABadEmail() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "shortpass")
                .param("email", "not-an-email")
                .param("password", "short")
                .param("confirmPassword", "short"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("registerForm", "password", "email"));

        assertThat(userRepository.findByUsernameIgnoreCase("shortpass")).isEmpty();
    }

    // ---- Log in / log out ----

    @Test
    void loginWorksWithUsernameAndWithEmail() throws Exception {
        userService.register("loginperson", "loginperson@example.com", "right-password");

        MvcResult byUsername = mockMvc.perform(post("/login")
                .param("usernameOrEmail", "loginperson")
                .param("password", "right-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"))
            .andReturn();
        assertThat(isLoggedIn(byUsername)).isTrue();

        MvcResult byEmail = mockMvc.perform(post("/login")
                .param("usernameOrEmail", "LoginPerson@Example.com")
                .param("password", "right-password"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        assertThat(isLoggedIn(byEmail)).isTrue();
    }

    @Test
    void loginWithTheWrongPasswordFailsAndDoesNotStartASession() throws Exception {
        userService.register("wrongpw", "wrongpw@example.com", "right-password");

        MvcResult result = mockMvc.perform(post("/login")
                .param("usernameOrEmail", "wrongpw")
                .param("password", "wrong-password"))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeHasErrors("loginForm"))
            .andReturn();

        assertThat(isLoggedIn(result)).isFalse();
    }

    @Test
    void loginWithAnUnknownUserFailsTheSameWay() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                .param("usernameOrEmail", "nobody-by-this-name")
                .param("password", "whatever-password"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("loginForm"))
            .andReturn();

        assertThat(isLoggedIn(result)).isFalse();
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        User user = userService.register("logoutperson", "logoutperson@example.com", "some-password");
        MockHttpSession session = sessionFor(user);

        mockMvc.perform(post("/logout").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(session.isInvalid()).isTrue();
    }

    // ---- Profile edit ----

    @Test
    void profileEditUpdatesTheAccountAndTheSessionName() throws Exception {
        User user = userService.register("beforename", "before@example.com", "some-password");
        MockHttpSession session = sessionFor(user);

        mockMvc.perform(post("/account/profile").session(session)
                .param("username", "aftername")
                .param("email", "after@example.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"));

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getUsername()).isEqualTo("aftername");
        assertThat(reloaded.getEmail()).isEqualTo("after@example.com");
        assertThat(session.getAttribute(SessionKeys.USERNAME)).isEqualTo("aftername");
    }

    @Test
    void profileEditRejectsAUsernameAnotherAccountUses() throws Exception {
        userService.register("alreadyused", "alreadyused@example.com", "some-password");
        User user = userService.register("editor", "editor@example.com", "some-password");

        mockMvc.perform(post("/account/profile").session(sessionFor(user))
                .param("username", "AlreadyUsed")
                .param("email", "editor@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeHasFieldErrors("profileForm", "username"));
    }

    @Test
    void profileEditRequiresLogin() throws Exception {
        mockMvc.perform(post("/account/profile")
                .param("username", "someone")
                .param("email", "someone@example.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"));
    }

    // ---- Account deletion ----

    @Test
    void deleteAccountNeedsTheRightPassword() throws Exception {
        User user = userService.register("keepme", "keepme@example.com", "right-password");

        mockMvc.perform(post("/account/delete").session(sessionFor(user))
                .param("password", "wrong-password"))
            .andExpect(status().isOk())
            .andExpect(view().name("account"))
            .andExpect(model().attributeExists("deleteError"));

        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void deleteAccountRemovesTheUserAndEndsTheSession() throws Exception {
        User user = userService.register("deleteme", "deleteme@example.com", "right-password");
        MockHttpSession session = sessionFor(user);

        mockMvc.perform(post("/account/delete").session(session)
                .param("password", "right-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void adminAccountsCannotBeDeletedFromTheProfilePage() throws Exception {
        User admin = userService.createAdmin("theadmin", "theadmin@example.com", "admin-password");

        mockMvc.perform(post("/account/delete").session(sessionFor(admin))
                .param("password", "admin-password"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("deleteError"));

        assertThat(userRepository.findById(admin.getId())).isPresent();
    }
}