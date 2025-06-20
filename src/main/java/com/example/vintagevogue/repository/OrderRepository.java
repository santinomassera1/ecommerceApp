package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Order;
import com.example.vintagevogue.model.OrderStatus;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Buscar por número de seguimiento
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    // Buscar pedidos de un usuario específico
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    // Buscar pedidos por estado
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
    
    // Buscar pedidos de un usuario por estado
    List<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status);
    
    // Buscar todos los pedidos ordenados por fecha (para admin)
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllOrdersByDateDesc();
    
    // Contar pedidos por estado (para estadísticas)
    Long countByStatus(OrderStatus status);
    
    // Contar pedidos de un usuario
    Long countByUser(User user);
} 