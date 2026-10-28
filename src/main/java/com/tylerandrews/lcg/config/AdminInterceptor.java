package com.tylerandrews.lcg.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.stereotype.Component;

import com.tylerandrews.lcg.repository.UserRepository;
import com.tylerandrews.lcg.model.User;

@Component 
public class AdminInterceptor implements HandlerInterceptor{
    
    private final UserRepository userRepository;

    public AdminInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/account");
            return false;
        }
        Object userKeyObject = session.getAttribute(SessionKeys.USER_ID);
        if (!(userKeyObject instanceof Long)) {
            response.sendRedirect("/account");
            return false;
        }
        Long longUserKey = ((Long)userKeyObject);
        var user = userRepository.findById(longUserKey);
        if (user.isEmpty()) {
            response.sendRedirect("/account");
            return false;
        }
        if (user.get().getRole() != User.Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
