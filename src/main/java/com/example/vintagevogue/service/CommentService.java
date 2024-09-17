package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Comment;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByProduct(Product product) {
        return commentRepository.findByProduct(product);
    }

    public Comment addComment(Product product, User user, String content) {
        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}