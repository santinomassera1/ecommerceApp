package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    void deleteByUsername(String username);
    List<User> findByRoles_NameAndUsernameContaining(String roleName, String username);
    List<User> findByUsernameContainingIgnoreCase(String name);
}
