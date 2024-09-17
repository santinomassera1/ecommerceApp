package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/manage-user")
    public String manageUserPage(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", userService.findAllRoles());
        return "manage-user";
    }

    @GetMapping("/admin/manage-ad")
    public String manageAds(Model model) {
        model.addAttribute("ad", new Ad());
        return "manage-ad";
    }

    @GetMapping("/manage-categories")
    public String manageCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "manage-categories";
    }

    @GetMapping("/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping("/assign-role")
    @ResponseBody
    public ResponseEntity<String> assignRole(@RequestParam String username, @RequestParam String roleName) {
        boolean success = userService.assignRoleToUser(username, roleName);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Role assigned successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error assigning role\"}");
        }
    }

    @PostMapping("/delete-user")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@RequestParam String username) {
        boolean success = userService.deleteUser(username);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"User deleted successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error deleting user\"}");
        }
    }

    @PostMapping("/block-user")
    @ResponseBody
    public ResponseEntity<String> blockUser(@RequestParam String username) {
        boolean success = userService.blockUser(username);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"User blocked successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error blocking user\"}");
        }
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/admin/manage-categories";
    }


    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/manage-categories";
    }
}


