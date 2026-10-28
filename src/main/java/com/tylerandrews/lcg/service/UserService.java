package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String rawPassword) {
        return create(username, email, rawPassword, User.Role.USER);
    }

    public User createAdmin(String username, String email, String rawPassword) {
        return create(username, email, rawPassword, User.Role.ADMIN);
    }

    private User create(String username, String email, String rawPassword, User.Role role) {
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    /** Returns the user only if the username-or-email exists and the password matches. */
    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || rawPassword == null) {
            return Optional.empty();
        }
        String id = usernameOrEmail.trim();
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(id, id)
            .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return rawPassword != null && passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /** True if another account (not exceptUserId) already uses this username, ignoring case. */
    public boolean usernameTaken(String username, Long exceptUserId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userRepository.findByUsernameIgnoreCase(username.trim())
            .filter(found -> !found.getId().equals(exceptUserId))
            .isPresent();
    }

    /** True if another account (not exceptUserId) already uses this email, ignoring case. */
    public boolean emailTaken(String email, Long exceptUserId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
            .filter(found -> !found.getId().equals(exceptUserId))
            .isPresent();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateProfile(User user, String username, String email) {
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }
}