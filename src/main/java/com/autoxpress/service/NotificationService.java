package com.autoxpress.service;

import com.autoxpress.model.NotificationModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.repository.NotificationRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createNotification(String username, String message, NotificationModel.NotificationType type) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        NotificationModel notification = new NotificationModel();
        notification.setNotificationUser(user);
        notification.setNotificationMessage(message);
        notification.setNotificationType(type);
        notification.setNotificationCreatedAt(LocalDateTime.now());
        notification.setNotificationRead(false);
        notificationRepository.save(notification);
    }

    public List<NotificationModel> getNotificationsByUser(Long userIdLong) {
        UserModel user = userRepository.findByUserId(userIdLong)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userIdLong));
        return notificationRepository.findByNotificationUser(user);
    }

    public long getUnreadNotificationCount(String username) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return notificationRepository.countByNotificationUserAndNotificationReadFalse(user);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.setNotificationRead(true);
        notificationRepository.save(notification);
    }

    public Page<NotificationModel> getNotificationsByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByNotificationUserUserId(userId, pageable);
    }
    
    @Transactional
    public void clearNotifications(Long userId) {
        notificationRepository.deleteByNotificationUserUserId(userId);
    }
}