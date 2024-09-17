package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String getProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("errorMessage", "User not found!");
            return "error";
        }

        // Asegúrate de que los productos se carguen con sus imágenes
        model.addAttribute("user", user);
        model.addAttribute("products", user.getProducts()); // Añade los productos asociados
        model.addAttribute("isOwnProfile", true);
        return "user-profile";
    }

    @GetMapping("/{userId}")
    public String viewUserProfile(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId).orElse(null);
    
        if (user == null) {
            model.addAttribute("errorMessage", "User not found");
            return "error";
        }
    
        model.addAttribute("user", user);
        model.addAttribute("products", user.getProducts()); // Añade los productos asociados
        model.addAttribute("isOwnProfile", false);
        return "user-profile";
    }

    @PostMapping("/update")
    public String updateProfile(User user, Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);

        if (currentUser != null) {
            currentUser.setEmail(user.getEmail());
            currentUser.setAddress(user.getAddress());
            currentUser.setCity(user.getCity());
            currentUser.setCountry(user.getCountry());
            userService.save(currentUser);
            model.addAttribute("successMessage", "Profile updated successfully!");
        } else {
            model.addAttribute("errorMessage", "User not found!");
        }

        return "redirect:/profile";
    }

    @PostMapping("/uploadImage")
    public String uploadProfileImage(@RequestParam("image") MultipartFile image, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user != null) {
            String imageUrl = userService.saveProfileImage(image, user);
            user.setProfileImageUrl(imageUrl);
            userService.save(user);
            model.addAttribute("successMessage", "Profile image uploaded successfully!");
        } else {
            model.addAttribute("errorMessage", "User not found!");
        }

        return "redirect:/profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                    @RequestParam("newPassword") String newPassword,
                                    @RequestParam("confirmPassword") String confirmPassword,
                                    Authentication authentication,
                                    Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user != null) {
            if (!userService.checkPassword(user, currentPassword)) {
                model.addAttribute("passwordError", "Current password is incorrect.");
                return "user-profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("passwordError", "Passwords do not match.");
                return "user-profile";
            }

            userService.updatePassword(user, newPassword);
            model.addAttribute("passwordSuccess", "Password saved correctly.");
        } else {
            model.addAttribute("errorMessage", "User not found!");
        }

        return "user-profile";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam("query") String query, Model model) {
        List<User> users = userService.searchUsersByName(query);
        model.addAttribute("users", users);
        return "user-search-results";
    }
}
