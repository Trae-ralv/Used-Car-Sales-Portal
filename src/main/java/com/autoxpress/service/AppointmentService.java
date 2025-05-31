package com.autoxpress.service;

import com.autoxpress.model.AppointmentModel;
import com.autoxpress.model.CarModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.repository.AppointmentRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveAppointment(AppointmentModel appointment, CarModel car, String username) throws Exception {
        Optional<UserModel> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new Exception("User not found.");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new Exception("Appointment date cannot be in the past.");
        }
        appointment.setAppointmentUser(user.get());
        appointment.setAppointmentCar(car);
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.PENDING);
        appointment.setAppointmentCreatedAt(LocalDateTime.now());
        appointment.setAppointmentUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentModel approveAppointment(Long appointmentId) throws Exception {
        AppointmentModel appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new Exception("Appointment not found."));
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.APPROVED);
        appointment.setAppointmentUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentModel denyAppointment(Long appointmentId) throws Exception {
        AppointmentModel appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new Exception("Appointment not found."));
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.DENIED);
        appointment.setAppointmentUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public List<AppointmentModel> getAppointmentsByUser(Long userId) {
        UserModel user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();
        return appointmentRepository.findByAppointmentUser(user);
    }

    public List<AppointmentModel> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        AppointmentModel appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    public AppointmentModel getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElse(null);
    }

    public Page<AppointmentModel> getAppointmentsByUser(Long userId, Pageable pageable) {
        UserModel user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return appointmentRepository.findByAppointmentUser(user, pageable);
    }
}