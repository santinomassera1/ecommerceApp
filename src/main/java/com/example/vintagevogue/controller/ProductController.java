package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;


    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "sell-product";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("image") MultipartFile imageFile,
                              Model model,
                              Authentication authentication) {
        try {
            // Obtener el nombre de usuario del usuario autenticado
            String username = authentication.getName();

            // Buscar al usuario en la base de datos usando el UserService
            User user = userService.findByUsername(username).orElseThrow(() ->
                    new IllegalArgumentException("User not found: " + username));

            // Asigna el usuario autenticado al producto
            product.setUser(user);

            // Guarda el producto junto con la imagen
            productService.saveProduct(product, imageFile);

            return "redirect:/products/my"; // Redirigir a la lista de productos del usuario
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error al guardar el producto. Inténtalo de nuevo.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "sell-product"; // Volver al formulario en caso de error
        }
    }

    @GetMapping("/my")
    public String manageProducts(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("products", productService.getProductsByUser(user));
        return "manage-products";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + id));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "sell-product";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products/my";
    }
}
