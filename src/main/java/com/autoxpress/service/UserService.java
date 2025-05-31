package com.autoxpress.service;

import com.autoxpress.model.PasswordResetTokenModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.model.NotificationModel;
import com.autoxpress.repository.PasswordResetTokenRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public UserModel registerUser(UserModel user) throws Exception {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new Exception("Username already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already exists.");
        }
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        user.setUserRole(UserModel.Role.USER);
        user.setUserCreatedAt(LocalDateTime.now());
        user.setUserUpdatedAt(LocalDateTime.now());
        UserModel savedUser = userRepository.save(user);
        notificationService.createNotification(
                user.getUsername(),
                "Welcome to AutoXpress! Your account has been created.",
                NotificationModel.NotificationType.GENERAL
        );
        return savedUser;
    }

    @Transactional
    public void requestPasswordReset(String email) throws Exception {
        Optional<UserModel> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserModel user = userOpt.get();
            String token = UUID.randomUUID().toString();
            PasswordResetTokenModel resetToken = new PasswordResetTokenModel();
            resetToken.setTokenUser(user);
            resetToken.setTokenValue(token);
            resetToken.setTokenExpiryDate(LocalDateTime.now().plusHours(1));
            resetToken.setTokenCreatedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
            notificationService.createNotification(
                    user.getUsername(),
                    "A password reset link has been sent to your email.",
                    NotificationModel.NotificationType.PASSWORD_RESET
            );
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetTokenModel resetToken = passwordResetTokenRepository.findByTokenValue(token)
                .orElseThrow(() -> new Exception("Invalid or expired token."));
        if (resetToken.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new Exception("Invalid or expired token.");
        }
        UserModel user = resetToken.getTokenUser();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        user.setUserUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        notificationService.createNotification(
                user.getUsername(),
                "Your password has been successfully reset.",
                NotificationModel.NotificationType.PASSWORD_RESET
        );
    }

    @Transactional
    public UserModel updateUserProfile(UserModel updatedUser, String username) throws Exception {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found."));
        user.setEmail(updatedUser.getEmail());
        user.setUserUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public UserModel getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public UserModel getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public void saveUser(UserModel user) {
        UserModel managedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + user.getUserId()));
        managedUser.setUsername(user.getUsername());
        managedUser.setEmail(user.getEmail());
        managedUser.setUserRole(user.getUserRole());
        managedUser.setUserUpdatedAt(user.getUserUpdatedAt());
        userRepository.saveAndFlush(managedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public UserModel markUserAsAdmin(Long userId) throws Exception {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found."));
        user.setUserRole(UserModel.Role.ADMIN);
        user.setUserUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public boolean isAdmin(Long userId) {
        UserModel user = userRepository.findById(userId).orElse(null);
        return user != null && user.getUserRole() == UserModel.Role.ADMIN;
    }
}