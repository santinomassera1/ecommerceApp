package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.CartItem;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.CartItemRepository;
import com.example.vintagevogue.repository.CartRepository;
import com.example.vintagevogue.repository.ProductRepository;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

 
  public Cart getCartByUser(User user) {
    Cart cart = cartRepository.findByUser(user);
    if (cart != null) {
        Hibernate.initialize(cart.getItems()); // Inicializa los items para evitar problemas de lazy loading
    } else {
        cart = new Cart();
        cart.setItems(new HashSet<CartItem>()); // Asegúrate de que los items no sean null
    }
    return cart;
}

  
    @Transactional
    public void addItemToCart(User user, Long productId, int quantity) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart); // Guardamos el carrito si es nuevo
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));

        // Verificar si el producto ya está en el carrito
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Incrementar la cantidad si ya existe en el carrito
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            BigDecimal updatedTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity()));
            existingItem.setTotalPrice(updatedTotalPrice);
        } else {
            // Si no está en el carrito, agregarlo como un nuevo CartItem
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            cartItem.setTotalPrice(totalPrice);

            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
    }

   
    @Transactional
    public void removeItemFromCart(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {

            // Buscar el CartItem en el carrito del usuario
            CartItem itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getId().equals(cartItemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

            // Verificar que el usuario sea el propietario del ítem
            if (!itemToRemove.getUser().equals(user)) {
                throw new IllegalStateException("User not authorized to remove this cart item");
            }

            // Eliminar el ítem del carrito
            cart.getItems().remove(itemToRemove);
            cartRepository.save(cart);
        } else {
            throw new IllegalStateException("Cart not found for user");
        }
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user);
        
        if (cart != null) {
            // Eliminar todos los ítems del carrito
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);  // Actualizar el carrito vacío
        }
    }

    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
