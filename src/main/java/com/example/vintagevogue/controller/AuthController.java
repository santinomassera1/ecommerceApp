package com.example.vintagevogue.controller;

import com.example.vintagevogue.custom.CustomException;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, String> response = new HashMap<>();
            response.put("username", authentication.getName());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());
        return "register-login";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> userMap) {
        String username = userMap.get("username");
        String email = userMap.get("email");
        String password = userMap.get("password");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        Map<String, Object> response = new HashMap<>();
        try {
            boolean newUser = userService.registerUser(user);
            if (newUser) {
                response.put("success", true);
                response.put("message", "Registration successful! Please check your email to verify your account.");
            } else {
                response.put("success", false);
                response.put("message", "Registration failed. Please try again.");
            }
        } catch (CustomException.UsernameAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", "Username is already taken. Please choose another one.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (CustomException.EmailAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", "Email is already in use. Please use another email.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An unexpected error occurred. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            model.addAttribute("verificationSuccess", true);
            model.addAttribute("message", "¡Tu cuenta ha sido verificada exitosamente! Ahora puedes iniciar sesión.");
        } else {
            model.addAttribute("verificationSuccess", false);
            model.addAttribute("message", "El enlace de verificación no es válido o ha expirado.");
        }
        model.addAttribute("user", new User());
        return "register-login";
    }

    @GetMapping("/verification-required")
    public String verificationRequired(Model model) {
        model.addAttribute("user", new User());
        return "verification-required";
    }

    @PostMapping("/resend-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendVerification(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String username = authentication.getName();
        boolean sent = userService.resendVerificationEmail(username);
        
        if (sent) {
            response.put("success", true);
            response.put("message", "Email de verificación enviado correctamente");
        } else {
            response.put("success", false);
            response.put("message", "No se pudo enviar el email de verificación");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("user", new User());
        return "forgot-passw";
    }

    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleForgotPassword(@RequestParam String email) {
        boolean success = userService.sendResetPasswordEmail(email);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "Se ha enviado un correo electrónico con instrucciones para restablecer tu contraseña");
        } else {
            response.put("success", false);
            response.put("message", "No se encontró ninguna cuenta con ese correo electrónico");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("user", new User());
        return "reset-password";
    }

    @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleResetPassword(@RequestParam String token, @RequestParam String newPassword, @RequestParam String confirmPassword) {
        Map<String, Object> response = new HashMap<>();
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Las contraseñas no coinciden");
            return ResponseEntity.ok(response);
        }
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            response.put("success", true);
            response.put("message", "Tu contraseña ha sido restablecida exitosamente");
        } else {
            response.put("success", false);
            response.put("message", "El enlace de restablecimiento no es válido o ha expirado");
        }
        return ResponseEntity.ok(response);
    }
    
}
