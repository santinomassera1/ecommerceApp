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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

 
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
    public List<CartItem> getCartItemsByUser(User user) {
        Cart cart = cartRepository.findByUser(user);
        return cart != null ? new ArrayList<>(cart.getItems()) : new ArrayList<>();
    }

    @Transactional
    public void processPurchase(User user) {
        Cart cart = cartRepository.findByUser(user);

        // Verificar si el carrito está vacío antes de proceder
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío. Agrega artículos antes de realizar la compra.");
        }

        // Obtener los IDs de los productos que se van a comprar
        List<Long> productIdsToMarkAsSold = cart.getItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        // Marcar los productos como vendidos (no disponibles)
        productService.markProductsAsSold(productIdsToMarkAsSold);

        // Si el carrito tiene artículos, procesar la compra
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }


    @Transactional
    public void addItemToCart(User user, Long productId, int quantity) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new HashSet<>()); // Inicializar los items en el carrito si es nuevo
            cartRepository.save(cart); // Guardamos el carrito si es nuevo
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));

        // Verificar si el producto está disponible antes de agregarlo al carrito
        if (!product.isAvailable()) {
            throw new IllegalStateException("Este producto ya no está disponible para la venta.");
        }

        // Verificar si el producto ya está en el carrito
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Si el producto ya está en el carrito, lanzar una excepción
            throw new IllegalStateException("Este producto ya está en tu carrito.");
        } else {
            // Si no está en el carrito, agregarlo como un nuevo CartItem
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setUser(user); // Asigna el usuario al CartItem si es obligatorio
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            // Calcular el precio total correctamente
            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            cartItem.setTotalPrice(totalPrice);

            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem); // Guardar el nuevo item
        }

        // Guardar el carrito actualizado
        cartRepository.save(cart);
    }



    @Transactional
    public void removeItemFromCart(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
            // Verificar que el cartItem pertenece al usuario antes de eliminarlo
            boolean itemExists = cart.getItems().stream()
                    .anyMatch(item -> item.getId().equals(cartItemId) && item.getUser().equals(user));

            if (!itemExists) {
                throw new IllegalArgumentException("Cart item not found or user not authorized");
            }

            // Remover el ítem del carrito usando ID para comparación
            // Gracias a orphanRemoval = true, esto eliminará automáticamente el CartItem de la BD
            cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
            cartRepository.save(cart);
        } else {
            throw new IllegalStateException("Cart not found for user");
        }
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user);

        if (cart != null) {
            // Eliminar todos los ítems del carrito SIN marcar productos como vendidos
            // Los productos solo se marcan como vendidos cuando se completa una compra real
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);  // Actualizar el carrito vacío
        }
    }

    // Método específico para compras instantáneas donde se quiere marcar el producto como vendido
    @Transactional
    public void processInstantPurchase(User user, Long productId) {
        // Marcar el producto específico como vendido
        productService.markProductAsSold(productId);
        
        // Limpiar cualquier referencia de este producto en el carrito del usuario
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
            cartRepository.save(cart);
        }
    }

    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
