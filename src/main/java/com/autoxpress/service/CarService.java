package com.autoxpress.service;

import com.autoxpress.model.CarImageModel;
import com.autoxpress.model.CarModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.repository.CarRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    private final String uploadDir = System.getProperty("user.home") + "/autoxpress/uploads/";

    public Page<CarModel> searchCars(
            String make, String carModel, Integer year, Double minPrice, Double maxPrice,
            Integer mileage, CarModel.FuelType fuelType, CarModel.TransmissionType transmission, Pageable pageable) {
        return carRepository.findCarsByCriteria(
                make, carModel, year, minPrice, maxPrice, mileage, fuelType, transmission, pageable);
    }

    public CarModel getCarById(Long id) {
        return carRepository.findById(id).orElse(null);
    }

    public List<CarModel> getCarsByUser(Long userId) {
        UserModel user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();
        return carRepository.findByCarUser(user);
    }

    public Page<CarModel> getCarsByUser(Long userId, Pageable pageable) {
        UserModel user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return carRepository.findByCarUser(user, pageable);
    }

    public List<CarModel> getAllCars() {
        return carRepository.findAll();
    }

    @Transactional
    public void saveCar(CarModel car, List<MultipartFile> images, String username) throws Exception {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found."));
        car.setCarUser(user);
        car.setCarStatus(CarModel.CarStatus.PENDING);
        car.setCarCreatedAt(LocalDateTime.now());
        car.setCarUpdatedAt(LocalDateTime.now());

        List<CarImageModel> carImages = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue; // Skip empty files
            }
            if (image.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new Exception("Image size exceeds 10MB.");
            }
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            image.transferTo(filePath.toFile());

            CarImageModel carImage = new CarImageModel();
            carImage.setImageCar(car);
            carImage.setImagePath("/images/uploads/" + fileName);
            carImage.setImageCreatedAt(LocalDateTime.now());
            carImages.add(carImage);
        }
        car.setCarImages(carImages);
        carRepository.save(car);
    }

    @Transactional
    public CarModel updateCar(CarModel updatedCar) {
        // Used by AdminController to update car details, excluding images
        CarModel existingCar = carRepository.findById(updatedCar.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + updatedCar.getCarId()));
        // Update fields that admin can modify
        existingCar.setCarMake(updatedCar.getCarMake());
        existingCar.setCarModel(updatedCar.getCarModel());
        existingCar.setRegistrationYear(updatedCar.getRegistrationYear());
        existingCar.setCarPrice(updatedCar.getCarPrice());
        existingCar.setCarMileage(updatedCar.getCarMileage());
        existingCar.setFuelType(updatedCar.getFuelType());
        existingCar.setTransmissionType(updatedCar.getTransmissionType());
        existingCar.setCarUpdatedAt(updatedCar.getCarUpdatedAt());
        // Do NOT update carImages (preserve existing images)
        return carRepository.save(existingCar);
    }

    @Transactional
    public CarModel activateCar(Long carId) throws Exception {
        CarModel car = carRepository.findById(carId)
                .orElseThrow(() -> new Exception("Car not found."));
        car.setCarStatus(CarModel.CarStatus.ACTIVE);
        car.setCarUpdatedAt(LocalDateTime.now());
        return carRepository.save(car);
    }

    @Transactional
    public CarModel deactivateCar(Long carId) throws Exception {
        CarModel car = carRepository.findById(carId)
                .orElseThrow(() -> new Exception("Car not found."));
        car.setCarStatus(CarModel.CarStatus.INACTIVE);
        car.setCarUpdatedAt(LocalDateTime.now());
        return carRepository.save(car);
    }

    @Transactional
    public CarModel transactSale(Long carId) throws Exception {
        CarModel car = carRepository.findById(carId)
                .orElseThrow(() -> new Exception("Car not found."));
        car.setCarStatus(CarModel.CarStatus.SOLD);
        car.setCarUpdatedAt(LocalDateTime.now());
        return carRepository.save(car);
    }

    @Transactional
    public void deleteCar(Long carId) {
        carRepository.deleteById(carId);
    }
}