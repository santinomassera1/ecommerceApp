package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.CartItem;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.CartItemRepository;
import com.example.vintagevogue.repository.CartRepository;
import com.example.vintagevogue.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
public class CartItemService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getCartItemsByUser(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public void addCartItem(User user, Long productId) {
        // Buscar el producto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));

        // Validar si el producto está disponible
        if (!product.isAvailable()) {
            throw new IllegalStateException("Este producto ya no está disponible para la venta.");
        }

        // Validar si el usuario que intenta agregar el producto es el mismo que lo publicó
        if (product.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("No puedes agregar tu propio producto al carrito.");
        }

        // Obtener el carrito del usuario
        Cart cart = cartRepository.findByUser(user);

        // Si el usuario no tiene carrito, crearlo
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        // Buscar si ya existe el CartItem para este producto en el carrito del usuario
        CartItem existingItem = cartItemRepository.findByUserAndProduct(user, product).orElse(null);

        if (existingItem != null) {
            // Si el producto ya está en el carrito, incrementar la cantidad
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            existingItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
            cartItemRepository.save(existingItem);
        } else {
            // Si el producto no está en el carrito, agregarlo
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setTotalPrice(product.getPrice());
            cartItem.setCart(cart); 
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void removeCartItem(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid cart item ID: " + cartItemId));
    
        // Verificación de autorización
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("User not authorized to remove this cart item");
        }
    
        // Obtener el carrito del usuario
        Cart cart = cartRepository.findByUser(user);
        
        if (cart != null) {
            // Remover el item de la colección del carrito usando ID para comparación
            // Gracias a orphanRemoval = true, esto eliminará automáticamente el CartItem de la BD
            cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
            
            // Guardar el carrito actualizado
            cartRepository.save(cart);
        } else {
            throw new IllegalStateException("Cart not found for user");
        }
    }

    @Transactional
    public void clearCartItems(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        cartItemRepository.deleteAll(cartItems);
    }
}