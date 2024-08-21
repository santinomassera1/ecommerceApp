package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Message;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndRecipient(User sender, User recipient);
    List<Message> findByRecipient(User recipient);
}
