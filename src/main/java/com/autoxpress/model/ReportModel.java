package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class ReportModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "report_user_id", nullable = false)
    private UserModel reportUser;

    @ManyToOne
    @JoinColumn(name = "report_car_id", nullable = false)
    private CarModel reportCar;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason", nullable = false)
    private ReportReason reportReason;

    @Column(name = "report_description")
    private String reportDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    @Column(name = "report_created_at", nullable = false)
    private LocalDateTime reportCreatedAt;

    @Column(name = "report_updated_at", nullable = false)
    private LocalDateTime reportUpdatedAt;

    public enum ReportReason {
        IRRELEVANT_PICTURE, MISLEADING_INFO, OTHER
    }

    public enum ReportStatus {
        PENDING, REVIEWED, RESOLVED
    }

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public UserModel getReportUser() {
		return reportUser;
	}

	public void setReportUser(UserModel reportUser) {
		this.reportUser = reportUser;
	}

	public CarModel getReportCar() {
		return reportCar;
	}

	public void setReportCar(CarModel reportCar) {
		this.reportCar = reportCar;
	}

	public ReportReason getReportReason() {
		return reportReason;
	}

	public void setReportReason(ReportReason reportReason) {
		this.reportReason = reportReason;
	}

	public String getReportDescription() {
		return reportDescription;
	}

	public void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}

	public ReportStatus getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(ReportStatus reportStatus) {
		this.reportStatus = reportStatus;
	}

	public LocalDateTime getReportCreatedAt() {
		return reportCreatedAt;
	}

	public void setReportCreatedAt(LocalDateTime reportCreatedAt) {
		this.reportCreatedAt = reportCreatedAt;
	}

	public LocalDateTime getReportUpdatedAt() {
		return reportUpdatedAt;
	}

	public void setReportUpdatedAt(LocalDateTime reportUpdatedAt) {
		this.reportUpdatedAt = reportUpdatedAt;
	}
    
    
}