package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Role;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.RoleRepository;
import com.example.vintagevogue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public boolean registerUser(User user) {
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return false;
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

    public User findByUsernameAndPassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
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
}
