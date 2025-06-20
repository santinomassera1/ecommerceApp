package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUser(User user);
    List<Product> findByNameContaining(String name);
    
    // Nuevos métodos para filtrar por disponibilidad
    List<Product> findByAvailableTrue();
    List<Product> findByUserAndAvailableTrue(User user);
    List<Product> findByNameContainingAndAvailableTrue(String name);
}
