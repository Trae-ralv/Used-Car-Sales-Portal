package com.autoxpress.repository;

import com.autoxpress.model.CarImageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarImageRepository extends JpaRepository<CarImageModel, Long> {
}