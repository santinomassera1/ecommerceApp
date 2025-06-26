package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CartService;
import com.example.vintagevogue.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
            
            cartService.addItemToCart(user, productId, quantity);
            
            response.put("success", true);
            response.put("message", "Producto agregado al carrito exitosamente.");
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ocurrió un error inesperado al agregar el producto.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 