package com.autoxpress.controller;

import com.autoxpress.model.CarModel;
import com.autoxpress.service.CarService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

	@Autowired
	private CarService carService;

	// Displays the homepage
	@GetMapping("/")
	public String home(Model model) {
		return "home";
	}

	// Displayes the car listing page with pagination and filtered results
	@GetMapping("/cars")
	public String carListing(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			@RequestParam(required = false) String make, @RequestParam(required = false) String carModel,
			@RequestParam(required = false) Integer year, @RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice, @RequestParam(required = false) Integer mileage,
			@RequestParam(required = false) String fuelType, @RequestParam(required = false) String transmission,
			Model model) {

		Pageable pageable = PageRequest.of(page, size);
		// Convert fuelType and transmission strings to enums
		CarModel.FuelType fuelTypeEnum = (fuelType != null && !fuelType.isEmpty()) ? CarModel.FuelType.valueOf(fuelType)
				: null;
		CarModel.TransmissionType transmissionEnum = (transmission != null && !transmission.isEmpty())
				? CarModel.TransmissionType.valueOf(transmission)
				: null;

		Page<CarModel> carPage = carService.searchCars(make, carModel, year, minPrice, maxPrice, mileage, fuelTypeEnum,
				transmissionEnum, pageable);

		model.addAttribute("cars", carPage.getContent());
		model.addAttribute("currentPage", carPage.getNumber());
		model.addAttribute("totalPages", carPage.getTotalPages());
		model.addAttribute("totalItems", carPage.getTotalElements());
		model.addAttribute("pageSize", size);

		// Retain search parameters for pagination links
		model.addAttribute("make", make);
		model.addAttribute("carModel", carModel);
		model.addAttribute("year", year);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
		model.addAttribute("mileage", mileage);
		model.addAttribute("fuelType", fuelType);
		model.addAttribute("transmission", transmission);

		// Enum values for filter dropdowns
		model.addAttribute("fuelTypes", Arrays.asList(CarModel.FuelType.values()));
		model.addAttribute("transmissionTypes", Arrays.asList(CarModel.TransmissionType.values()));

		return "car-listing";
	}

	// Handles AJAX requests for car search, returning paginated results as JSON
	@GetMapping("/cars/search")
	@ResponseBody
	public Map<String, Object> searchCarsAjax(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String make,
			@RequestParam(required = false) String carModel, @RequestParam(required = false) Integer year,
			@RequestParam(required = false) Double minPrice, @RequestParam(required = false) Double maxPrice,
			@RequestParam(required = false) Integer mileage, @RequestParam(required = false) String fuelType,
			@RequestParam(required = false) String transmission) {

		Map<String, Object> response = new HashMap<>();
		try {
			Pageable pageable = PageRequest.of(page, size);
			// Convert fuelType and transmission strings to enums
			CarModel.FuelType fuelTypeEnum = (fuelType != null && !fuelType.isEmpty())
					? CarModel.FuelType.valueOf(fuelType)
					: null;
			CarModel.TransmissionType transmissionEnum = (transmission != null && !transmission.isEmpty())
					? CarModel.TransmissionType.valueOf(transmission)
					: null;

			Page<CarModel> carPage = carService.searchCars(make, carModel, year, minPrice, maxPrice, mileage,
					fuelTypeEnum, transmissionEnum, pageable);

			response.put("cars", carPage.getContent());
			response.put("currentPage", carPage.getNumber());
			response.put("totalPages", carPage.getTotalPages());
			response.put("totalItems", carPage.getTotalElements());
			response.put("pageSize", size);
		} catch (Exception e) {
			System.err.println("Error in /cars/search: " + e.getMessage());
			e.printStackTrace();
			response.put("cars", List.of());
			response.put("currentPage", 0);
			response.put("totalPages", 0);
			response.put("totalItems", 0L);
			response.put("pageSize", size);
		}
		return response;
	}

	// Display About Us Page
	@GetMapping("/about")
	public String aboutPage() {
		return "about-us";
	}

	// Display Contact Us Page
	@GetMapping("/contact")
	public String contactPage(Model model) {
		return "contact-us";
	}

}