package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CarController {

    @Autowired
    private CarService carService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    // Displays the details of a specific car by ID
    @GetMapping("/cars/{id}")
    public String carDetails(@PathVariable Long id, Model model) {

        CarModel car = carService.getCarById(id);
        if (car == null) {
            return "redirect:/cars?error=CarNotFound";
        }
        
        model.addAttribute("car", car);
        model.addAttribute("appointment", new AppointmentModel());
        model.addAttribute("report", new ReportModel());

        return "car-details";
    }

    // Shows the form for posting a new car
    @GetMapping("/cars/post")
    public String showPostCarForm(Model model, Authentication authentication) {
    	
    	String userId = authentication.getName();
    	Long userIdLong = Long.parseLong(userId);
    	UserModel user = userService.getUserById(userIdLong);
    	
        model.addAttribute("car", new CarModel());
        model.addAttribute("fuelTypes", CarModel.FuelType.values());
        model.addAttribute("transmissionTypes", CarModel.TransmissionType.values());
        Long notificationCount = notificationService.getUnreadNotificationCount(user.getUsername());
        model.addAttribute("notificationCount", notificationCount);
        return "post-car";
    }

    // Handles the submission of a new car listing with images
    @PostMapping("/cars/post")
    public String postCar(
            @Valid @ModelAttribute("car") CarModel car,
            BindingResult result,
            @RequestParam("images") List<MultipartFile> images,
            Model model,
            Authentication authentication) throws Exception {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel user = userService.getUserById(userIdLong);
        if (user == null) {
            return "redirect:/login";
        }
        if (result.hasErrors() || images.size() < 1 || images.size() > 5) {
            model.addAttribute("fuelTypes", CarModel.FuelType.values());
            model.addAttribute("transmissionTypes", CarModel.TransmissionType.values());
            model.addAttribute("error", images.size() < 1 ? "At least one image is required." :
                    images.size() > 5 ? "Maximum 5 images allowed." : null);
            Long notificationCount = notificationService.getUnreadNotificationCount(user.getUsername());
            model.addAttribute("notificationCount", notificationCount);
            return "post-car";
        }
        try {
            carService.saveCar(car, images, user.getUsername());
            notificationService.createNotification(
                    user.getUsername(),
                    "Car submitted for approval: " + car.getCarMake() + " " + car.getCarModel(),
                    NotificationModel.NotificationType.GENERAL
            );
            return "redirect:/user/dashboard?submitted";
        } catch (Exception e) {
            model.addAttribute("fuelTypes", CarModel.FuelType.values());
            model.addAttribute("transmissionTypes", CarModel.TransmissionType.values());
            model.addAttribute("error", "Failed to post car: " + e.getMessage());
            Long notificationCount = notificationService.getUnreadNotificationCount(user.getUsername());
            model.addAttribute("notificationCount", notificationCount);
            return "post-car";
        }
    }
    
    
    // Books a test drive appointment for a specific car
    @PostMapping("/cars/{id}/appointment")
    public String bookAppointment(
            @PathVariable Long id,
            @Valid @ModelAttribute("appointment") AppointmentModel appointment,
            BindingResult result,
            Model model,
            Authentication authentication) throws Exception {
    	
    	String userId = authentication.getName();
    	Long userIdLong = Long.parseLong(userId);
    	UserModel user = userService.getUserById(userIdLong);
    	
        CarModel car = carService.getCarById(id);
        
        if (car == null || result.hasErrors()) {
            model.addAttribute("car", car);
            model.addAttribute("appointment", appointment);
            model.addAttribute("report", new ReportModel());
            model.addAttribute("error", car == null ? "Car not found." : "Invalid appointment details.");
            Long notificationCount = notificationService.getUnreadNotificationCount(user.getUsername());
            model.addAttribute("notificationCount", notificationCount);
            return "car-details";
        }
        appointmentService.saveAppointment(appointment, car, user.getUsername());
        notificationService.createNotification(
        		user.getUsername(),
                "Test drive appointment booked for " + car.getCarMake() + " " + car.getCarModel(),
                NotificationModel.NotificationType.APPOINTMENT_STATUS
        );
        return "redirect:/cars/" + id + "?appointmentBooked";
    }

    // Submits a report for a specific car listing
    @PostMapping("/cars/{id}/report")
    public String submitReport(
            @PathVariable Long id,
            @Valid @ModelAttribute("report") ReportModel report,
            BindingResult result,
            Model model,
            Authentication authentication) throws Exception {
    	
    	String userId = authentication.getName();
    	Long userIdLong = Long.parseLong(userId);
    	UserModel user = userService.getUserById(userIdLong);
        CarModel car = carService.getCarById(id);
        
        if (car == null || result.hasErrors()) {
            model.addAttribute("car", car);
            model.addAttribute("appointment", new AppointmentModel());
            model.addAttribute("report", report);
            model.addAttribute("error", car == null ? "Car not found." : "Invalid report.");
            Long notificationCount = notificationService.getUnreadNotificationCount(user.getUsername());
            model.addAttribute("notificationCount", notificationCount);
            return "car-details";
        }
        reportService.saveReport(report, car, user.getUsername());
        notificationService.createNotification(
        		user.getUsername(),
                "Report submitted for " + car.getCarMake() + " " + car.getCarModel(),
                NotificationModel.NotificationType.GENERAL
        );
        return "redirect:/cars/" + id + "?reportSubmitted";
    }
}