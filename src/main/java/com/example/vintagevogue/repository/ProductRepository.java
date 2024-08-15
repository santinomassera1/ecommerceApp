package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p JOIN FETCH p.user")
    List<Product> findAllWithUsers();
    List<Product> findByUser(User user);

}
