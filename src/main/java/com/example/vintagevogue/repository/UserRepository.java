package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    boolean existsByEmail(String email);
<<<<<<< HEAD
}
=======
}
>>>>>>> fb5bd05981b960023b76268448945f182399185d
