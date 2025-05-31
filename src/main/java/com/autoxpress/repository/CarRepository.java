package com.autoxpress.repository;

import com.autoxpress.model.CarModel;
import com.autoxpress.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findByCarUser(UserModel user);

    Page<CarModel> findByCarUser(UserModel user, Pageable pageable);

    @Query("SELECT c FROM CarModel c WHERE " +
           "c.carStatus = 'ACTIVE' AND " +
           "(:make IS NULL OR LOWER(c.carMake) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:carModel IS NULL OR LOWER(c.carModel) LIKE LOWER(CONCAT('%', :carModel, '%'))) AND " +
           "(:year IS NULL OR c.registrationYear = :year) AND " +
           "(:minPrice IS NULL OR c.carPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.carPrice <= :maxPrice) AND " +
           "(:mileage IS NULL OR c.carMileage <= :mileage) AND " +
           "(:fuelType IS NULL OR c.fuelType = :fuelType) AND " +
           "(:transmission IS NULL OR c.transmissionType = :transmission)")
    Page<CarModel> findCarsByCriteria(
            @Param("make") String make,
            @Param("carModel") String carModel,
            @Param("year") Integer year,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("mileage") Integer mileage,
            @Param("fuelType") CarModel.FuelType fuelType,
            @Param("transmission") CarModel.TransmissionType transmission,
            Pageable pageable);
}