package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Comment;
import com.example.vintagevogue.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProduct(Product product);
}