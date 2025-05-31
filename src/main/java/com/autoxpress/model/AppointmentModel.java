package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class AppointmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @ManyToOne
    @JoinColumn(name = "appointment_user_id", nullable = false)
    private UserModel appointmentUser;

    @ManyToOne
    @JoinColumn(name = "appointment_car_id", nullable = false)
    private CarModel appointmentCar;

    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", nullable = false)
    private AppointmentStatus appointmentStatus;

    @Column(name = "appointment_created_at", nullable = false)
    private LocalDateTime appointmentCreatedAt;

    @Column(name = "appointment_updated_at", nullable = false)
    private LocalDateTime appointmentUpdatedAt;

    public enum AppointmentStatus {
        PENDING, APPROVED, DENIED, CANCELED
    }
    
	public Long getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Long appointmentId) {
		this.appointmentId = appointmentId;
	}

	public UserModel getAppointmentUser() {
		return appointmentUser;
	}

	public void setAppointmentUser(UserModel appointmentUser) {
		this.appointmentUser = appointmentUser;
	}

	public CarModel getAppointmentCar() {
		return appointmentCar;
	}

	public void setAppointmentCar(CarModel appointmentCar) {
		this.appointmentCar = appointmentCar;
	}

	public LocalDateTime getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDateTime appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public AppointmentStatus getAppointmentStatus() {
		return appointmentStatus;
	}

	public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}

	public LocalDateTime getAppointmentCreatedAt() {
		return appointmentCreatedAt;
	}

	public void setAppointmentCreatedAt(LocalDateTime appointmentCreatedAt) {
		this.appointmentCreatedAt = appointmentCreatedAt;
	}

	public LocalDateTime getAppointmentUpdatedAt() {
		return appointmentUpdatedAt;
	}

	public void setAppointmentUpdatedAt(LocalDateTime appointmentUpdatedAt) {
		this.appointmentUpdatedAt = appointmentUpdatedAt;
	}
    
    
}