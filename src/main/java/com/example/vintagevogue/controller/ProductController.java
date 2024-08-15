package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ImageStorageService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
    private ImageStorageService imageStorageService;

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "sell-product";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product, RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("message", "Product saved successfully");
            return "redirect:/products/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save product");
            return "redirect:/products/new";
        }
    }

    @GetMapping("/my")
    public String manageProducts(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("productId") Long productId, RedirectAttributes redirectAttributes) {
        // Llamar al servicio para manejar la lógica de guardado
        String imageUrl = productService.saveProductImage(file, productId);

        // Redirigir o devolver la respuesta adecuada
        redirectAttributes.addFlashAttribute("message", "Imagen subida correctamente");
        return "redirect:/product/" + productId;
    }
}
