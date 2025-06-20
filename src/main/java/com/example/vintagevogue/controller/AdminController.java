package com.example.vintagevogue.controller;

import com.example.vintagevogue.dto.UserDTO;
import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.UserRepository;
import com.example.vintagevogue.service.AdService;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdService adService;

    @GetMapping
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/manage-user")
    public String manageUserPage(Model model) {
        model.addAttribute("users", userService.findAllUsers().stream().map(UserDTO::new).toList());
        model.addAttribute("roles", userService.findAllRoles());
        return "manage-user";
    }

    @GetMapping("/manage-ad")
    public String manageAds(Model model) {
        model.addAttribute("ads", adService.getAllAds());
        model.addAttribute("ad", new Ad());
        return "manage-ad";
    }

    @GetMapping("/manage-categories")
    public String manageCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "manage-categories";
    }

    /**  Buscar usuarios por username */
    @GetMapping("/search-user")
    @ResponseBody
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<UserDTO> userDTOs = users.stream().map(UserDTO::new).toList();
        return ResponseEntity.ok(userDTOs);
    }

    /**  Obtener todos los usuarios */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        List<UserDTO> userDTOs = users.stream().map(UserDTO::new).toList();
        return ResponseEntity.ok(userDTOs);
    }

    @PostMapping(value = "/assign-role", consumes = "application/json", produces = "application/json")  // 💡 Forzar JSON
    @ResponseBody
    public ResponseEntity<String> assignRole(@RequestBody UserDTO userDTO) {
        System.out.println("📌 Recibido: " + userDTO.getUsername() + " - " + userDTO.getEmail());

        boolean success = userService.assignRoleToUser(userDTO.getUsername(), userDTO.getEmail()); // Asegúrate de que `getEmail()` contiene el rol o cámbialo
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Role assigned successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error assigning role\"}");
        }
    }

    /**  Eliminar un usuario */
    @DeleteMapping("/delete-user")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@RequestBody UserDTO userDTO) {
        System.out.println(" Eliminando usuario: " + userDTO.getUsername()); // Debugging
        boolean success = userService.deleteUser(userDTO.getUsername());
        if (success) {
            return ResponseEntity.ok("{\"message\":\"User deleted successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error deleting user\"}");
        }
    }

    /** 🚫 Bloquear usuario */
    @PostMapping("/block-user")
    @ResponseBody
    public ResponseEntity<String> blockUser(@RequestParam String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\":\"Invalid username\"}");
        }

        boolean success = userService.blockUser(username);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"User blocked successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error blocking user\"}");
        }
    }

    /** 📂 Crear nueva categoría */
    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "category-form";
    }

    /** 💾 Guardar nueva categoría */
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/admin/manage-categories";
    }

    /** ❌ Eliminar categoría */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/manage-categories";
    }
}
