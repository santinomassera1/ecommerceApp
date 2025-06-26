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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String addCartItem(@PathVariable Long productId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        try {
            cartItemService.addCartItem(user, productId);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito exitosamente.");
            return "redirect:/cart";
        } catch (IllegalStateException e) {
            // Si el producto no está disponible, el usuario intenta agregar su propio producto,
            // o el producto ya está en el carrito
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Si el error es que el producto ya está en el carrito, redirigir al carrito
            if (e.getMessage().contains("ya está en tu carrito")) {
                return "redirect:/cart";
            }
            return "redirect:/home";
        } catch (Exception e) {
            // Manejar cualquier otra excepción no esperada
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al agregar el producto.");
            return "redirect:/home";
        }
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeCartItem(@PathVariable Long cartItemId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        try {
            cartItemService.removeCartItem(user, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito.");
            return "redirect:/cart";
        } catch (IllegalStateException e) {
            // Si el usuario no está autorizado para eliminar el ítem
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            // Manejar cualquier otra excepción no esperada
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado.");
            return "redirect:/cart";
        }
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));
        
        try {
            // Llamar al servicio para vaciar el carrito
            cartService.clearCart(user);
            redirectAttributes.addFlashAttribute("success", "Carrito vaciado exitosamente.");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al vaciar el carrito.");
            return "redirect:/cart";
        }
    }
}