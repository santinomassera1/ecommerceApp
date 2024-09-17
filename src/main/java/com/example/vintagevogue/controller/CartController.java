package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.CartItem;
import com.example.vintagevogue.service.CartService;
import com.example.vintagevogue.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Objects;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;


    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        Cart cart = cartService.getCartByUser(user);
        if (cart != null) {
            BigDecimal totalPrice = cart.getItems().stream()
                    .map(CartItem::getTotalPrice)
                    .filter(Objects::nonNull) // Filtrar valores nulos
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalPrice", totalPrice);
        } else {
            model.addAttribute("totalPrice", BigDecimal.ZERO);
        }
        model.addAttribute("cart", cart != null ? cart : new Cart());
        
        return "cart";
    }

}