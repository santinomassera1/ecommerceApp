package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByProductId(Long productId);
}