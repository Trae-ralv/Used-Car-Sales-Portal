package com.autoxpress.repository;

import com.autoxpress.model.NotificationModel;
import com.autoxpress.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {

    // Find all notifications for a specific user (non-paginated)
    List<NotificationModel> findByNotificationUser(UserModel user);

    // Find paginated notifications for a specific user by userId
    Page<NotificationModel> findByNotificationUserUserId(Long userId, Pageable pageable);

    // Count unread notifications for a specific user
    long countByNotificationUserAndNotificationReadFalse(UserModel user);

    // Count unread notifications for a specific user by username (for NotificationService.getUnreadNotificationCount)
    long countByNotificationUserUsernameAndNotificationReadFalse(String username);
    
    // Delete all notifications for a specific user by userId
    void deleteByNotificationUserUserId(Long userId);
}