package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Comment;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CommentService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addComment(@RequestParam Long productId, @RequestParam String content, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productService.getProductById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        commentService.addComment(product, user, content);
        return "redirect:/products/details/" + productId;
    }

    @GetMapping("/product/{productId}")
    public String getComments(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        List<Comment> comments = commentService.getCommentsByProduct(product);
        model.addAttribute("comments", comments);
        return "product-details";
    }
}