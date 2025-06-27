package com.example.vintagevogue.controller;

import com.example.vintagevogue.dto.UserDTO;
import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.UserRepository;
import com.example.vintagevogue.service.AdService;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

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
    
    @Autowired
    private ProductService productService;
    
    @Value("${app.maintenance.enabled:false}")
    private boolean maintenanceMode;

    @GetMapping
    public String adminPage(Model model) {
        // Inyectar servicios en el modelo para acceso desde Thymeleaf
        model.addAttribute("userService", userService);
        model.addAttribute("categoryService", categoryService);
        model.addAttribute("adService", adService);
        model.addAttribute("productService", productService);
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

    @GetMapping("/manage-product")
    public String manageProducts(Model model) {
        List<Product> products = productService.getAllProductsIncludingUnavailable();
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manage-product";
    }
    
    @PostMapping("/toggle-maintenance")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleMaintenanceMode() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Leer el archivo de propiedades
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            
            // Cambiar el valor de la propiedad
            boolean currentMode = Boolean.parseBoolean(properties.getProperty("app.maintenance.enabled", "false"));
            boolean newMode = !currentMode;
            properties.setProperty("app.maintenance.enabled", String.valueOf(newMode));
            
            // Guardar el archivo de propiedades
            String appPropertiesPath = getClass().getClassLoader().getResource("application.properties").getPath();
            try (FileOutputStream out = new FileOutputStream(appPropertiesPath)) {
                properties.store(out, "Updated maintenance mode");
            }
            
            // Actualizar el valor en memoria
            maintenanceMode = newMode;
            
            response.put("success", true);
            response.put("enabled", newMode);
            response.put("message", newMode ? 
                    "Modo mantenimiento activado. Los usuarios verán un mensaje de mantenimiento." : 
                    "Modo mantenimiento desactivado. La aplicación funciona normalmente.");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el modo de mantenimiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/maintenance-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMaintenanceStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", maintenanceMode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/mark-as-sold")
    @ResponseBody
    public ResponseEntity<String> markProductAsSold(@RequestParam Long productId) {
        try {
            Optional<Product> productOptional = productService.getProductById(productId);
            if (productOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"Producto no encontrado\"}");
            }
            
            productService.markProductAsSold(productId);
            return ResponseEntity.ok("{\"message\":\"Producto marcado como vendido correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Error al marcar el producto como vendido: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/products/mark-as-available")
    @ResponseBody
    public ResponseEntity<String> markProductAsAvailable(@RequestParam Long productId) {
        try {
            Optional<Product> productOptional = productService.getProductById(productId);
            if (productOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"Producto no encontrado\"}");
            }
            
            Product product = productOptional.get();
            product.setAvailable(true);
            productService.saveProductWithoutImages(product);
            return ResponseEntity.ok("{\"message\":\"Producto marcado como disponible correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Error al marcar el producto como disponible: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/products/delete")
    @ResponseBody
    public ResponseEntity<String> deleteProduct(@RequestParam Long productId) {
        try {
            Optional<Product> productOptional = productService.getProductById(productId);
            if (productOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"Producto no encontrado\"}");
            }
            
            productService.deleteProduct(productId);
            return ResponseEntity.ok("{\"message\":\"Producto eliminado correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Error al eliminar el producto: " + e.getMessage() + "\"}");
        }
    }

    /**  Buscar usuarios por username */
    @GetMapping("/search-user")
    @ResponseBody
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            List<User> users = userService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            List<UserDTO> userDTOs = users.stream().map(UserDTO::new).toList();
            return ResponseEntity.ok(userDTOs);
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
        System.out.println("📌 Recibido: " + userDTO.getUsername() + " - " + userDTO.getRoleName());

        boolean success = userService.assignRoleToUser(userDTO.getUsername(), userDTO.getRoleName());
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Rol asignado correctamente\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error al asignar rol\"}");
        }
    }
    
    @PostMapping(value = "/remove-role", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> removeRole(@RequestBody UserDTO userDTO) {
        System.out.println("📌 Removiendo rol: " + userDTO.getUsername() + " - " + userDTO.getRoleName());

        boolean success = userService.removeRoleFromUser(userDTO.getUsername(), userDTO.getRoleName());
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Rol removido correctamente\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error al remover rol. El usuario no tiene ese rol o no existe.\"}");
        }
    }

    /**  Eliminar un usuario */
    @DeleteMapping("/delete-user")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@RequestBody UserDTO userDTO) {
        System.out.println(" Eliminando usuario: " + userDTO.getUsername()); // Debugging
        boolean success = userService.deleteUser(userDTO.getUsername());
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Usuario eliminado correctamente\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error al eliminar usuario\"}");
        }
    }

    /** 🚫 Bloquear usuario */
    @PostMapping("/block-user")
    @ResponseBody
    public ResponseEntity<String> blockUser(@RequestParam String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\":\"Nombre de usuario inválido\"}");
        }

        boolean success = userService.blockUser(username);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Usuario bloqueado correctamente\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error al bloquear usuario\"}");
        }
    }

    /** 📂 Crear nueva categoría */
    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "category-form";
    }

    /** 📝 Editar categoría existente */
    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no válida con ID: " + id));
        model.addAttribute("category", category);
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
