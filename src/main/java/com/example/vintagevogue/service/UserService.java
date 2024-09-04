package com.example.vintagevogue.service;

import com.example.vintagevogue.custom.CustomException;
import com.example.vintagevogue.model.Role;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.RoleRepository;
import com.example.vintagevogue.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Primary
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public boolean registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new CustomException.UsernameAlreadyExistsException("Username already exists.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new CustomException.EmailAlreadyExistsException("Address already exists.");
        }

        if (user.getPassword() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName("ROLE_USER");
        if (role != null) {
            roles.add(role);
        }
        user.setRoles(roles);

        userRepository.save(user);
        sendVerificationEmail(user);
        return true;
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification";
        String confirmationUrl = "http://localhost:8080/auth/verify?token=" + user.getVerificationToken();
        String message = "Click the link to verify your email: " + confirmationUrl;

        emailService.sendSimpleEmail(user.getEmail(), subject, message);
    }

    public boolean sendResetPasswordEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        String subject = "Reset Password";
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + user.getVerificationToken();
        String message = "Click the link to reset your password: " + resetUrl;

        emailService.sendSimpleEmail(user.getEmail(), subject, message);
        return true;
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public List<User> searchUsersByName(String name) {
        return userRepository.findByUsernameContainingIgnoreCase(name);
    }

    @Transactional
    public List<User> searchUsersForMessaging(String username) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        users.forEach(user -> Hibernate.initialize(user.getProducts()));
        return users;
    }
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationToken(null); // Clear the token after successful reset
        userRepository.save(user);
        return true;
    }

    public boolean assignRoleToUser(String username, String roleName) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            return false;
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public boolean deleteUser(String username) {
        userRepository.deleteByUsername(username);
        return false;
    }

    @Transactional
    public boolean blockUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
        }
        return false;
    }

    public String saveProfileImage(MultipartFile image, User user) {
        try {
            String folder = "uploads/";
            byte[] bytes = image.getBytes();
            Path path = Paths.get(folder + user.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes);
            return "/uploads/" + user.getId() + "_" + image.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean checkPassword(User user, String currentPassword) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
