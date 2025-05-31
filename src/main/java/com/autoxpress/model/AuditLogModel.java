package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLogModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne
    @JoinColumn(name = "audit_admin_id", nullable = false)
    private UserModel auditAdmin;

    @Column(name = "audit_action", nullable = false)
    private String auditAction;

    @Column(name = "audit_entity_type")
    private String auditEntityType;

    @Column(name = "audit_entity_id")
    private Long auditEntityId;

    @Column(name = "audit_created_at", nullable = false)
    private LocalDateTime auditCreatedAt;

	public Long getAuditId() {
		return auditId;
	}

	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

	public UserModel getAuditAdmin() {
		return auditAdmin;
	}

	public void setAuditAdmin(UserModel auditAdmin) {
		this.auditAdmin = auditAdmin;
	}

	public String getAuditAction() {
		return auditAction;
	}

	public void setAuditAction(String auditAction) {
		this.auditAction = auditAction;
	}

	public String getAuditEntityType() {
		return auditEntityType;
	}

	public void setAuditEntityType(String auditEntityType) {
		this.auditEntityType = auditEntityType;
	}

	public Long getAuditEntityId() {
		return auditEntityId;
	}

	public void setAuditEntityId(Long auditEntityId) {
		this.auditEntityId = auditEntityId;
	}

	public LocalDateTime getAuditCreatedAt() {
		return auditCreatedAt;
	}

	public void setAuditCreatedAt(LocalDateTime auditCreatedAt) {
		this.auditCreatedAt = auditCreatedAt;
	}
    
    
}