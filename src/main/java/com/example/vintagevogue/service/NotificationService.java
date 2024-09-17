package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Notification;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public List<Notification> findByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
