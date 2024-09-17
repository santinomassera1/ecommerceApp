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

        // Validar si el usuario que intenta agregar el producto es el mismo que lo publicó
        if (product.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot add your own product to the cart.");
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
    
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCartItems(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        cartItemRepository.deleteAll(cartItems);
    }
}