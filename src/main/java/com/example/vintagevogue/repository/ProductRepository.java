package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}