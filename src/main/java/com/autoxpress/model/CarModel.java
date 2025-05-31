package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "cars")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    @ManyToOne
    @JoinColumn(name = "car_user_id", nullable = false)
    private UserModel carUser;

    @Column(name = "car_make", nullable = false)
    private String carMake;

    @Column(name = "car_model", nullable = false)
    private String carModel;

    @Column(name = "registration_year", nullable = false)
    private Integer registrationYear;

    @Column(name = "car_price", nullable = false)
    private Double carPrice;

    @Column(name = "car_mileage")
    private Integer carMileage;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type")
    private TransmissionType transmissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_status", nullable = false)
    private CarStatus carStatus;

    @Column(name = "car_created_at", nullable = false)
    private LocalDateTime carCreatedAt;

    @Column(name = "car_updated_at", nullable = false)
    private LocalDateTime carUpdatedAt;

    @OneToMany(mappedBy = "imageCar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CarImageModel> carImages;

    public enum FuelType {
        PETROL, DIESEL, ELECTRIC, HYBRID
    }

    public enum TransmissionType {
        MANUAL, AUTOMATIC
    }

    public enum CarStatus {
        PENDING, ACTIVE, INACTIVE, SOLD
    }

	public Long getCarId() {
		return carId;
	}

	public void setCarId(Long carId) {
		this.carId = carId;
	}

	public UserModel getCarUser() {
		return carUser;
	}

	public void setCarUser(UserModel carUser) {
		this.carUser = carUser;
	}

	public String getCarMake() {
		return carMake;
	}

	public void setCarMake(String carMake) {
		this.carMake = carMake;
	}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public Integer getRegistrationYear() {
		return registrationYear;
	}

	public void setRegistrationYear(Integer registrationYear) {
		this.registrationYear = registrationYear;
	}

	public Double getCarPrice() {
		return carPrice;
	}

	public void setCarPrice(Double carPrice) {
		this.carPrice = carPrice;
	}

	public Integer getCarMileage() {
		return carMileage;
	}

	public void setCarMileage(Integer carMileage) {
		this.carMileage = carMileage;
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public TransmissionType getTransmissionType() {
		return transmissionType;
	}

	public void setTransmissionType(TransmissionType transmissionType) {
		this.transmissionType = transmissionType;
	}

	public CarStatus getCarStatus() {
		return carStatus;
	}

	public void setCarStatus(CarStatus carStatus) {
		this.carStatus = carStatus;
	}

	public LocalDateTime getCarCreatedAt() {
		return carCreatedAt;
	}

	public void setCarCreatedAt(LocalDateTime carCreatedAt) {
		this.carCreatedAt = carCreatedAt;
	}

	public LocalDateTime getCarUpdatedAt() {
		return carUpdatedAt;
	}

	public void setCarUpdatedAt(LocalDateTime carUpdatedAt) {
		this.carUpdatedAt = carUpdatedAt;
	}

	public List<CarImageModel> getCarImages() {
		return carImages;
	}

	public void setCarImages(List<CarImageModel> carImages) {
		this.carImages = carImages;
	}
    
}
