package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser(User user);
}