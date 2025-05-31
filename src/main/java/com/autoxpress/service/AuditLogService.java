package com.autoxpress.service;

import com.autoxpress.model.AuditLogModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.repository.AuditLogRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void logAction(String username, String actionDescription, String entityType, Long entityId) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        AuditLogModel auditLog = new AuditLogModel();
        auditLog.setAuditAdmin(user);
        auditLog.setAuditAction(actionDescription);
        auditLog.setAuditEntityType(entityType);
        auditLog.setAuditEntityId(entityId);
        auditLog.setAuditCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}