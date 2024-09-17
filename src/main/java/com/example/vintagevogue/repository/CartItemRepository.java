package com.example.vintagevogue.repository;
import com.example.vintagevogue.model.CartItem;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    Optional<CartItem> findByUserAndProduct(User user, Product product);
}
