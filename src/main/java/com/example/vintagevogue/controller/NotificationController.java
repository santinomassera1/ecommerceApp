package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Notification;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.NotificationService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Notification> notifications = notificationService.findByUser(user);
            return ResponseEntity.ok(notifications);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markNotificationsAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAsRead(currentUser);
        return ResponseEntity.ok().build();
    }
}
