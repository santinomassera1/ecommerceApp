package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Message;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.MessageService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getMessages(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Message> messages = messageService.getMessagesForUser(user);
        model.addAttribute("messages", messages);
        return "messaging";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("recipient") Long recipientId,
                              @RequestParam("content") String content,
                              Authentication authentication) {
        User sender = (User) authentication.getPrincipal();
        User recipient = userService.findById(recipientId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        messageService.sendMessage(sender, recipient, content);
        return "redirect:/messages";
    }
}
