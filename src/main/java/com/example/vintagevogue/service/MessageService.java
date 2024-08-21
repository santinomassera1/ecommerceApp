package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Message;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message sendMessage(User sender, User recipient, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getConversation(User sender, User recipient) {
        return messageRepository.findBySenderAndRecipient(sender, recipient);
    }

    public List<Message> getMessagesForUser(User recipient) {
        return messageRepository.findByRecipient(recipient);
    }
}
