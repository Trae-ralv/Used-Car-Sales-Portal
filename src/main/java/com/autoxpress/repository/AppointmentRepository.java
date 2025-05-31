package com.autoxpress.repository;

import com.autoxpress.model.AppointmentModel;
import com.autoxpress.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentModel, Long> {
    List<AppointmentModel> findByAppointmentUser(UserModel user);

    Page<AppointmentModel> findByAppointmentUser(UserModel user, Pageable pageable);
}