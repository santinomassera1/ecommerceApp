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
    public String login() {
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
        return "forgot-passw";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public String handleForgotPassword(@RequestParam String email) {
        boolean success = userService.sendResetPasswordEmail(email);
        if (success) {
            return "{\"success\":true,\"message\":\"Reset email sent successfully\"}";
        } else {
            return "{\"success\":false,\"message\":\"Email not found\"}";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    @ResponseBody
    public String handleResetPassword(@RequestParam String token, @RequestParam String newPassword, @RequestParam String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return "{\"success\":false,\"message\":\"Passwords do not match\"}";
        }
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return "{\"success\":true,\"message\":\"Password reset successful\"}";
        } else {
            return "{\"success\":false,\"message\":\"Error resetting password\"}";
        }
    }

}