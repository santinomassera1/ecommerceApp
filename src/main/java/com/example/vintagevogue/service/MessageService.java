package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Message;
import com.example.vintagevogue.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getConversation(String fromUser, String toUser) {
        List<Message> sentMessages = messageRepository.findByFromUserAndToUserOrderByTimestampAsc(fromUser, toUser);
        List<Message> receivedMessages = messageRepository.findByToUserAndFromUserOrderByTimestampAsc(fromUser, toUser);

        sentMessages.addAll(receivedMessages);
        sentMessages.sort(Comparator.comparing(Message::getTimestamp));

        return sentMessages;
    }
}
