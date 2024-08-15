package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRepository extends JpaRepository<Ad, Long> {
}
