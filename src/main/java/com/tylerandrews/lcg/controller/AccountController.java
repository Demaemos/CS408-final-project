package com.tylerandrews.lcg.controller;

import com.tylerandrews.lcg.config.SessionKeys;
import com.tylerandrews.lcg.form.LoginForm;
import com.tylerandrews.lcg.form.ProfileForm;
import com.tylerandrews.lcg.form.RegisterForm;
import com.tylerandrews.lcg.model.User;
import com.tylerandrews.lcg.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // ---- The Account page: login/register tabs, or the profile when logged in ----

    @GetMapping("/account")
    public String account(HttpSession session, Model model) {
        Optional<User> user = currentUser(session);
        if (user.isPresent()) {
            prepareProfile(model, user.get());
        } else {
            prepareLoggedOut(model, "login");
        }
        return "account";
    }

    // ---- Register / log in / log out ----

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result,
                           HttpServletRequest request,
                           Model model) {
        if (request.getSession(false) != null && request.getSession(false).getAttribute(SessionKeys.USER_ID) != null) {
            return "redirect:/account";
        }

        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (userService.usernameTaken(form.getUsername(), null)) {
            result.rejectValue("username", "taken", "That username is already taken.");
        }
        if (userService.emailTaken(form.getEmail(), null)) {
            result.rejectValue("email", "taken", "An account with that email already exists.");
        }
        if (result.hasErrors()) {
            prepareLoggedOut(model, "register");
            return "account";
        }

        User user = userService.register(form.getUsername(), form.getEmail(), form.getPassword());
        startSession(request, user);
        return "redirect:/account";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult result,
                        HttpServletRequest request,
                        Model model) {
        if (request.getSession(false) != null && request.getSession(false).getAttribute(SessionKeys.USER_ID) != null) {
            return "redirect:/account";
        }

        if (!result.hasErrors()) {
            Optional<User> user = userService.authenticate(form.getUsernameOrEmail(), form.getPassword());
            if (user.isPresent()) {
                startSession(request, user.get());
                return "redirect:/account";
            }
            // Same message whether the username or the password was wrong.
            result.reject("login.failed", "Invalid username/email or password.");
        }
        prepareLoggedOut(model, "login");
        return "account";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ---- Profile edit and account deletion ----

    @PostMapping("/account/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult result,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirect) {
        Optional<User> current = currentUser(session);
        if (current.isEmpty()) {
            return "redirect:/account";
        }
        User user = current.get();

        if (userService.usernameTaken(form.getUsername(), user.getId())) {
            result.rejectValue("username", "taken", "That username is already taken.");
        }
        if (userService.emailTaken(form.getEmail(), user.getId())) {
            result.rejectValue("email", "taken", "An account with that email already exists.");
        }
        if (result.hasErrors()) {
            prepareProfile(model, user);
            return "account";
        }

        User updated = userService.updateProfile(user, form.getUsername(), form.getEmail());
        session.setAttribute(SessionKeys.USERNAME, updated.getUsername());
        redirect.addFlashAttribute("message", "Profile updated.");
        return "redirect:/account";
    }

    @PostMapping("/account/delete")
    public String deleteAccount(@RequestParam(defaultValue = "") String password,
                                HttpSession session,
                                Model model) {
        Optional<User> current = currentUser(session);
        if (current.isEmpty()) {
            return "redirect:/account";
        }
        User user = current.get();

        if (user.getRole() == User.Role.ADMIN) {
            model.addAttribute("deleteError", "Admin accounts can't be deleted from here.");
            prepareProfile(model, user);
            return "account";
        }
        if (!userService.passwordMatches(user, password)) {
            model.addAttribute("deleteError", "Incorrect password. Your account was not deleted.");
            prepareProfile(model, user);
            return "account";
        }

        userService.delete(user);
        session.invalidate();
        return "redirect:/";
    }

    // ---- Helpers ----

    private Optional<User> currentUser(HttpSession session) {
        Object id = session.getAttribute(SessionKeys.USER_ID);
        if (!(id instanceof Long userId)) {
            return Optional.empty();
        }
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            session.invalidate(); // the account no longer exists
        }
        return user;
    }

    /** Replaces any old session with a fresh one, so a session id from before login can't be reused. */
    private void startSession(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USERNAME, user.getUsername());
        session.setAttribute(SessionKeys.ROLE, user.getRole().name());
    }

    private void prepareLoggedOut(Model model, String activeTab) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        model.addAttribute("activeTab", activeTab);
    }

    private void prepareProfile(Model model, User user) {
        model.addAttribute("user", user);
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new ProfileForm(user.getUsername(), user.getEmail()));
        }
    }
}