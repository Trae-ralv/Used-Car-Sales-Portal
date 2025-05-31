package com.autoxpress.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "car_images")
public class CarImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "image_car_id", nullable = false)
    @JsonBackReference
    private CarModel imageCar;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "image_created_at", nullable = false)
    private LocalDateTime imageCreatedAt;

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public CarModel getImageCar() {
		return imageCar;
	}

	public void setImageCar(CarModel imageCar) {
		this.imageCar = imageCar;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public LocalDateTime getImageCreatedAt() {
		return imageCreatedAt;
	}

	public void setImageCreatedAt(LocalDateTime imageCreatedAt) {
		this.imageCreatedAt = imageCreatedAt;
	}
    
    
}