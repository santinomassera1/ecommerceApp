package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;
import com.example.vintagevogue.model.Comment; 
import com.example.vintagevogue.service.CommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List; 

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
    private CommentService commentService;

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "sell-product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElseThrow(() ->
                    new IllegalArgumentException("User not found: " + username));

            product.setUser(user);
            productService.saveProductWithoutImages(product); // Guardar el producto sin imágenes

            // Redirigir al formulario de subida de imágenes, pasando el ID del producto
            return "redirect:/products/uploadImages?productId=" + product.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "sell-product";
        }
    }

    @GetMapping("/uploadImages")
    public String uploadImagesForm(@RequestParam("productId") Long productId, Model model) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        
        model.addAttribute("product", product);
        return "upload-image";
    }

    @PostMapping("/uploadImages")
    public String uploadImages(@RequestParam("productId") Long productId,
                            @RequestParam("images") MultipartFile[] imageFiles, Model model) {
        try {
            productService.saveImagesForProduct(productId, imageFiles);
            return "redirect:/profile";  // Redirige al listado de productos del usuario
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to upload images.");
            return "upload-image";
        }
    }

    @GetMapping("/my")
    public String manageProducts(Model model, Authentication authentication) {
        // Obtener el nombre de usuario del objeto Authentication
        String username = authentication.getName();
 
        // Busca el usuario en tu base de datos usando el nombre de usuario
        com.example.vintagevogue.model.User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Obtener los productos del usuario
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

    @GetMapping("/details/{productId}")
    public String getProductDetails(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        
        List<Comment> comments = commentService.getCommentsByProduct(product);
        model.addAttribute("product", product);
        model.addAttribute("comments", comments);
        return "product-details";
    }

}
