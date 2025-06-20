package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Order;
import com.example.vintagevogue.model.OrderItem;
import com.example.vintagevogue.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Buscar items de una orden específica
    List<OrderItem> findByOrder(Order order);
    
    // Buscar items que contienen un producto específico
    List<OrderItem> findByProduct(Product product);
} 