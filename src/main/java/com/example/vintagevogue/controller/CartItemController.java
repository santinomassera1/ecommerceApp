package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CartItemService;
import com.example.vintagevogue.service.CartService;
import com.example.vintagevogue.service.UserService;

import java.util.Collections;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart/items")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCartItems(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        // Obtener el carrito y forzar la inicialización de los ítems
        Cart cart = cartService.getCartByUser(user);
        
        if (cart != null) {

            Hibernate.initialize(cart.getItems());
            model.addAttribute("cartItems", cart.getItems());
        } else {
            model.addAttribute("cartItems", Collections.emptyList());
        }

        return "cart";
    }

    @PostMapping("/add/{productId}")
    public String addCartItem(@PathVariable Long productId, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        try {
            cartItemService.addCartItem(user, productId);
            return "redirect:/cart";
        } catch (IllegalStateException e) {
            // Si el usuario intenta agregar su propio producto, mostrar un mensaje de error
            model.addAttribute("error", e.getMessage());
            return "cart"; // Reenvía a la vista de carrito con el mensaje de error
        } catch (Exception e) {
            // Manejar cualquier otra excepción no esperada
            model.addAttribute("error", "An unexpected error occurred.");
            return "cart";
        }
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeCartItem(@PathVariable Long cartItemId, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        try {
            cartItemService.removeCartItem(user, cartItemId);
            return "redirect:/cart";
        } catch (IllegalStateException e) {
            // Si el usuario no está autorizado para eliminar el ítem
            model.addAttribute("error", e.getMessage());
            return "cart";
        } catch (Exception e) {
            // Manejar cualquier otra excepción no esperada
            model.addAttribute("error", "An unexpected error occurred.");
            return "cart";
        }
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        // Llamar al servicio para vaciar el carrito
        cartService.clearCart(user);
        
        return "redirect:/cart"; // Redirigir nuevamente a la vista del carrito
    }
}