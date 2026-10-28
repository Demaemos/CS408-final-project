package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerStoresABcryptHashNotThePassword() {
        User user = userService.register("hashtester", "Hash@Example.com", "plain-password-1");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getPasswordHash()).isNotEqualTo("plain-password-1").startsWith("$2");
        assertThat(passwordEncoder.matches("plain-password-1", user.getPasswordHash())).isTrue();
        assertThat(user.getEmail()).isEqualTo("hash@example.com");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void createAdminGivesTheAdminRole() {
        User admin = userService.createAdmin("adminperson", "adminperson@example.com", "admin-pass-1");
        assertThat(admin.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void authenticateAcceptsUsernameOrEmailIgnoringCase() {
        userService.register("LoginTester", "login@example.com", "right-password");

        assertThat(userService.authenticate("logintester", "right-password")).isPresent();
        assertThat(userService.authenticate("LOGIN@example.com", "right-password")).isPresent();
    }

    @Test
    void authenticateRejectsWrongPasswordAndUnknownUser() {
        userService.register("wrongpass", "wrongpass@example.com", "right-password");

        assertThat(userService.authenticate("wrongpass", "wrong-password")).isEmpty();
        assertThat(userService.authenticate("nobody-here", "right-password")).isEmpty();
        assertThat(userService.authenticate("", "right-password")).isEmpty();
        assertThat(userService.authenticate("wrongpass", null)).isEmpty();
    }

    @Test
    void usernameAndEmailTakenIgnoreCaseAndCanExcludeTheOwner() {
        User user = userService.register("TakenName", "taken@example.com", "some-password");

        assertThat(userService.usernameTaken("takenname", null)).isTrue();
        assertThat(userService.emailTaken("TAKEN@example.com", null)).isTrue();
        assertThat(userService.usernameTaken("takenname", user.getId())).isFalse();
        assertThat(userService.emailTaken("taken@example.com", user.getId())).isFalse();
        assertThat(userService.usernameTaken("someone-else", null)).isFalse();
    }

    @Test
    void updateProfileChangesUsernameAndEmail() {
        User user = userService.register("oldname", "old@example.com", "some-password");

        userService.updateProfile(user, "  newname ", "New@Example.com");

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getUsername()).isEqualTo("newname");
        assertThat(reloaded.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void deleteRemovesTheUser() {
        User user = userService.register("deleteme", "deleteme@example.com", "some-password");
        Long id = user.getId();

        userService.delete(user);

        assertThat(userRepository.findById(id)).isEmpty();
    }
}