package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Message;
import com.example.vintagevogue.model.Notification;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.MessageService;
import com.example.vintagevogue.service.NotificationService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/messages/history")
    @ResponseBody
    public List<Message> getConversationHistory(@RequestParam String fromUser, @RequestParam String toUser) {
        return messageService.getConversation(fromUser, toUser);
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        System.out.println("Message received from server: " + message.getContent());

        Message savedMessage = messageService.saveMessage(message);

        Notification notification = new Notification();
        notification.setContent("New message from: " + message.getFromUser());
        notification.setUser(userService.findByUsername(message.getToUser()).get());
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notificationService.save(notification);
        return savedMessage;
    }

    @GetMapping("/messages")
    public String showMessagingPage() {
        return "messaging";
    }


    @GetMapping("/messages/{userId}")
    public String showMessagingPageWithUser(@PathVariable Long userId, Model model) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
            return "messaging";
        } else {
            return "error/404";
        }
    }

}
