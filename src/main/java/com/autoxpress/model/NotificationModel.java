package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class NotificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "notification_user_id", nullable = false)
    private UserModel notificationUser;

    @Column(name = "notification_message", nullable = false)
    private String notificationMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "notification_created_at", nullable = false)
    private LocalDateTime notificationCreatedAt;

    @Column(name = "notification_read")
    private Boolean notificationRead;

    public enum NotificationType {
        BID_UPDATE, APPOINTMENT_STATUS, PASSWORD_RESET, GENERAL
    }

	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public UserModel getNotificationUser() {
		return notificationUser;
	}

	public void setNotificationUser(UserModel notificationUser) {
		this.notificationUser = notificationUser;
	}

	public String getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public LocalDateTime getNotificationCreatedAt() {
		return notificationCreatedAt;
	}

	public void setNotificationCreatedAt(LocalDateTime notificationCreatedAt) {
		this.notificationCreatedAt = notificationCreatedAt;
	}

	public Boolean getNotificationRead() {
		return notificationRead;
	}

	public void setNotificationRead(Boolean notificationRead) {
		this.notificationRead = notificationRead;
	}
    
    
}