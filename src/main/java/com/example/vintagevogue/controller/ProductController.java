package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ImageService;
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

    @Autowired
    private ImageService imageService;

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "sell-product";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("images") MultipartFile[] imageFiles,
                              Model model,
                              Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElseThrow(() ->
                    new IllegalArgumentException("User not found: " + username));

            // Set the user to the product
            product.setUser(user);

            // Save the product first to generate the ID
            productService.saveProduct(product, imageFiles);

            return "redirect:/products/my";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to save product.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "sell-product";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "sell-product";
        }
    }

    @GetMapping("/my")
    public String manageProducts(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("products", productService.getProductsByUser(user));
        return "manage-product";
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
