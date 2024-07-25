package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());
        return "register-login";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> userMap) {
        String username = userMap.get("username");
        String email = userMap.get("email");
        String password = userMap.get("password");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        boolean newUser = userService.registerUser(user);
        Map<String, Object> response = new HashMap<>();
        if (newUser) {
            response.put("success", true);
            response.put("message", "Registration successful! Please check your email to verify your account.");
        } else {
            response.put("success", false);
            response.put("message", "Registration failed. Please try again.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("user", new User());
        return "forgot-passw";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleForgotPassword(@RequestParam String email) {
        boolean success = userService.sendResetPasswordEmail(email);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "Reset email sent successfully");
        } else {
            response.put("success", false);
            response.put("message", "Email not found");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("user", new User());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleResetPassword(@RequestParam String token, @RequestParam String newPassword, @RequestParam String confirmPassword) {
        Map<String, Object> response = new HashMap<>();
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Passwords do not match");
            return ResponseEntity.ok(response);
        }
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            response.put("success", true);
            response.put("message", "Password reset successful");
        } else {
            response.put("success", false);
            response.put("message", "Invalid token");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody Map<String, String> loginMap) {
        String username = loginMap.get("username");
        String password = loginMap.get("password");

        User user = userService.findByUsernameAndPassword(username, password);
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            if (!user.isVerified()) {
                response.put("success", false);
                response.put("message", "Email not verified. Please check your email.");
            } else {
                response.put("success", true);
                response.put("message", "Login successful!");
            }
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
        }
        return ResponseEntity.ok(response);
    }

}