package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
}