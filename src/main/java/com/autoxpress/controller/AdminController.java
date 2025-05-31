package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminController {

	@Autowired
	private UserService userService;

	@Autowired
	private CarService carService;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private AuditLogService auditLogService;

	// Displays the admin dashboard with user, car, appointment, and report data
	@GetMapping("/admin/dashboard")
	public String adminDashboard(Model model, Authentication authentication) {
		String userId = authentication.getName();
		Long userIdLong = Long.parseLong(userId);
		if (!userService.isAdmin(userIdLong)) {
			return "redirect:/login";
		}
		UserModel user = userService.getUserById(userIdLong);
		if (user == null) {
			return "redirect:/login";
		}
		model.addAttribute("user", user);
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("cars", carService.getAllCars());
		model.addAttribute("appointments", appointmentService.getAllAppointments());
		model.addAttribute("reports", reportService.getAllReports());
		model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(user.getUsername()));
		return "admin-dashboard";
	}

	// Marks a user as an admin and logs the action
	@PostMapping("/admin/users/mark-admin")
	public String markUserAsAdmin(@RequestParam("userId") Long userId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			UserModel user = userService.markUserAsAdmin(userId);
			UserModel admin = userService.getUserById(adminUserIdLong);
			auditLogService.logAction(admin.getUsername(), "Marked user as admin: " + user.getUsername(), "User",
					userId);
			notificationService.createNotification(user.getUsername(), "You have been assigned admin privileges.",
					NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?adminMarked";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=UserNotFound";
		}
	}

	// Demotes an admin to a regular user and logs the action
	@PostMapping("/admin/users/make-user")
	public String makeUser(@RequestParam("userId") Long userId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			UserModel user = userService.getUserById(userId);
			if (user == null) {
				return "redirect:/admin/dashboard?error=UserNotFound";
			}
			UserModel admin = userService.getUserById(adminUserIdLong);
			if (admin == null || user.getUserId().equals(adminUserIdLong)) {
				return "redirect:/admin/dashboard?error=CannotDemoteSelf";
			}
			user.setUserRole(UserModel.Role.USER);
			user.setUserUpdatedAt(LocalDateTime.now());
			userService.saveUser(user);
			auditLogService.logAction(admin.getUsername(), "Demoted user to USER: " + user.getUsername(), "User",
					userId);
			notificationService.createNotification(user.getUsername(),
					"Your admin privileges have been removed. You are now a regular user.",
					NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?userDemoted";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=DemotionFailed";
		}
	}

	// Deletes a user (except admins) via AJAX and logs the action
	@PostMapping("/admin/users/delete")
	@ResponseBody
	public ResponseEntity<Map<String, String>> deleteUser(@RequestParam("userId") Long userId, Authentication authentication) {
	    Map<String, String> response = new HashMap<>();
	    String adminUserId = authentication.getName();
	    Long adminUserIdLong = Long.parseLong(adminUserId);
	    if (!userService.isAdmin(adminUserIdLong)) {
	        response.put("status", "error");
	        response.put("message", "Unauthorized access.");
	        return ResponseEntity.status(403).body(response);
	    }
	    try {
	        UserModel user = userService.getUserById(userId);
	        if (user == null) {
	            response.put("status", "error");
	            response.put("message", "User not found.");
	            return ResponseEntity.status(404).body(response);
	        }
	        if (user.getUserRole() == UserModel.Role.ADMIN) {
	            response.put("status", "error");
	            response.put("message", "Cannot delete an admin user.");
	            return ResponseEntity.status(400).body(response);
	        }
	        UserModel admin = userService.getUserById(adminUserIdLong);
	        userService.deleteUser(userId);
	        auditLogService.logAction(admin.getUsername(), "Deleted user: " + user.getUsername(), "User", userId);
	        response.put("status", "success");
	        response.put("message", "User deleted successfully.");
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        response.put("status", "error");
	        response.put("message", "Failed to delete user: " + e.getMessage());
	        return ResponseEntity.status(500).body(response);
	    }
	}
	// Edits a user’s details via AJAX and logs the action
	@PostMapping("/admin/users/edit")
	@ResponseBody
	public ResponseEntity<Map<String, String>> editUser(@RequestParam("userId") Long userId,
			@RequestParam("username") String username, @RequestParam("email") String email,
			Authentication authentication) {
		Map<String, String> response = new HashMap<>();
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			response.put("status", "error");
			response.put("message", "Unauthorized access.");
			return ResponseEntity.status(403).body(response);
		}
		try {
			UserModel existingUser = userService.getUserById(userId);
			if (existingUser == null) {
				response.put("status", "error");
				response.put("message", "User not found.");
				return ResponseEntity.status(404).body(response);
			}
			UserModel admin = userService.getUserById(adminUserIdLong);
			existingUser.setUsername(username);
			existingUser.setEmail(email);
			existingUser.setUserUpdatedAt(LocalDateTime.now());
			userService.saveUser(existingUser);
			auditLogService.logAction(admin.getUsername(), "Edited user: " + username, "User", userId);
			notificationService.createNotification(username, "Your profile has been updated by an admin.",
					NotificationModel.NotificationType.GENERAL);
			response.put("status", "success");
			response.put("message", "User updated successfully.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Failed to edit user: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}

	// Edits a car’s details via AJAX, updates its status, and logs the action
	@PostMapping("/admin/cars/edit")
	@ResponseBody
	public ResponseEntity<Map<String, String>> editCar(@Valid @ModelAttribute("car") CarModel car, BindingResult result,
			@RequestParam("carStatus") String carStatus, Authentication authentication) {
		Map<String, String> response = new HashMap<>();
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			response.put("status", "error");
			response.put("message", "Unauthorized access.");
			return ResponseEntity.status(403).body(response);
		}
		if (result.hasErrors()) {
			response.put("status", "error");
			response.put("message", "Invalid input data.");
			return ResponseEntity.badRequest().body(response);
		}
		try {
			CarModel existingCar = carService.getCarById(car.getCarId());
			if (existingCar == null) {
				response.put("status", "error");
				response.put("message", "Car not found.");
				return ResponseEntity.status(404).body(response);
			}
			// Validate status transition (e.g., can't go from SOLD to ACTIVE)
			CarModel.CarStatus newStatus = CarModel.CarStatus.valueOf(carStatus);
			if (existingCar.getCarStatus() == CarModel.CarStatus.SOLD && newStatus != CarModel.CarStatus.SOLD) {
				response.put("status", "error");
				response.put("message", "Cannot change status of a sold car.");
				return ResponseEntity.status(400).body(response);
			}
			UserModel admin = userService.getUserById(adminUserIdLong);
			existingCar.setCarMake(car.getCarMake());
			existingCar.setCarModel(car.getCarModel());
			existingCar.setRegistrationYear(car.getRegistrationYear());
			existingCar.setCarPrice(car.getCarPrice());
			existingCar.setCarMileage(car.getCarMileage());
			existingCar.setFuelType(car.getFuelType());
			existingCar.setTransmissionType(car.getTransmissionType());
			existingCar.setCarStatus(newStatus); // Update carStatus
			existingCar.setCarUpdatedAt(LocalDateTime.now());
			carService.updateCar(existingCar);
			auditLogService.logAction(admin.getUsername(), "Edited car: " + car.getCarMake() + " " + car.getCarModel(),
					"Car", car.getCarId());
			notificationService.createNotification(existingCar.getCarUser().getUsername(),
					"Your car listing has been updated by an admin: " + car.getCarMake() + " " + car.getCarModel(),
					NotificationModel.NotificationType.GENERAL);
			response.put("status", "success");
			response.put("message", "Car updated successfully.");
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			response.put("status", "error");
			response.put("message", "Invalid status value: " + carStatus);
			return ResponseEntity.status(400).body(response);
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Failed to edit car: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}

	// Deletes a car via AJAX, notifies the owner, and logs the action
	@PostMapping("/admin/cars/delete")
	@ResponseBody
	public ResponseEntity<Map<String, String>> deleteCar(@RequestParam("carId") Long carId,
			@RequestParam("reason") String reason, Authentication authentication) {
		Map<String, String> response = new HashMap<>();
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			response.put("status", "error");
			response.put("message", "Unauthorized access.");
			return ResponseEntity.status(403).body(response);
		}
		try {
			CarModel car = carService.getCarById(carId);
			if (car == null) {
				response.put("status", "error");
				response.put("message", "Car not found.");
				return ResponseEntity.status(404).body(response);
			}
			UserModel admin = userService.getUserById(adminUserIdLong);
			String carOwnerUsername = car.getCarUser().getUsername();
			carService.deleteCar(carId);
			auditLogService.logAction(admin.getUsername(),
					"Deleted car: " + car.getCarMake() + " " + car.getCarModel() + " (Reason: " + reason + ")", "Car",
					carId);
			notificationService.createNotification(carOwnerUsername, "Admin has removed this car. Reason: " + reason,
					NotificationModel.NotificationType.GENERAL);
			response.put("status", "success");
			response.put("message", "Car deleted successfully.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Failed to delete car: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}

	// Activates a car listing if the price is reasonable and logs the action
	@PostMapping("/admin/cars/activate")
	public String activateCar(@RequestParam("carId") Long carId, Authentication authentication) {
		System.out.println("Activating car with ID: " + carId);
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			System.out.println("User is not an admin: " + adminUserIdLong);
			return "redirect:/login";
		}
		try {
			CarModel car = carService.getCarById(carId);
			if (car == null) {
				System.out.println("Car not found: " + carId);
				return "redirect:/admin/dashboard?error=CarNotFound";
			}
			if (car.getCarPrice() < 2000) {
				System.out.println("Unreasonable price for car ID " + carId + ": " + car.getCarPrice());
				return "redirect:/admin/dashboard?error=UnreasonablePrice&price=" + car.getCarPrice();
			}
			UserModel admin = userService.getUserById(adminUserIdLong);
			carService.activateCar(carId);
			auditLogService.logAction(admin.getUsername(),
					"Approved car listing: " + car.getCarMake() + " " + car.getCarModel(), "Car", carId);
			notificationService.createNotification(car.getCarUser().getUsername(),
					"Your car listing has been approved: " + car.getCarMake() + " " + car.getCarModel(),
					NotificationModel.NotificationType.GENERAL);
			System.out.println("Car activated successfully: " + carId);
			return "redirect:/admin/dashboard?carApproved";
		} catch (Exception e) {
			System.out.println("Error activating car ID " + carId + ": " + e.getMessage());
			return "redirect:/admin/dashboard?error=CarNotFound";
		}
	}

	// Deactivates a car listing and logs the action
	@PostMapping("/admin/cars/deactivate")
	public String deactivateCar(@RequestParam("carId") Long carId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			CarModel car = carService.deactivateCar(carId);
			UserModel admin = userService.getUserById(adminUserIdLong);
			auditLogService.logAction(admin.getUsername(),
					"Deactivated car: " + car.getCarMake() + " " + car.getCarModel(), "Car", carId);
			notificationService.createNotification(car.getCarUser().getUsername(),
					"Your car has been deactivated: " + car.getCarMake() + " " + car.getCarModel(),
					NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?carDeactivated";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=CarNotFound";
		}
	}

	// Marks a car as sold and logs the action
	@PostMapping("/admin/cars/sell")
	public String sellCar(@RequestParam("carId") Long carId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			CarModel car = carService.transactSale(carId);
			UserModel admin = userService.getUserById(adminUserIdLong);
			auditLogService.logAction(admin.getUsername(),
					"Marked car as sold: " + car.getCarMake() + " " + car.getCarModel(), "Car", carId);
			notificationService.createNotification(car.getCarUser().getUsername(),
					"Your car has been sold: " + car.getCarMake() + " " + car.getCarModel(),
					NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?carSold";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=CarNotFound";
		}
	}

	// Approves a test drive appointment and logs the action
	@PostMapping("/admin/appointments/approve")
	public String approveAppointment(@RequestParam("appointmentId") Long appointmentId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			AppointmentModel appointment = appointmentService.approveAppointment(appointmentId);
			UserModel admin = userService.getUserById(adminUserIdLong);
			auditLogService.logAction(admin.getUsername(),
					"Approved appointment for car ID: " + appointment.getAppointmentCar().getCarId(), "Appointment",
					appointmentId);
			notificationService.createNotification(appointment.getAppointmentUser().getUsername(),
					"Your test drive appointment has been approved.",
					NotificationModel.NotificationType.APPOINTMENT_STATUS);
			return "redirect:/admin/dashboard?appointmentApproved";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=AppointmentNotFound";
		}
	}

	// Denies a test drive appointment and logs the action
	@PostMapping("/admin/appointments/deny")
	public String denyAppointment(@RequestParam("appointmentId") Long appointmentId, Authentication authentication) {
		String adminUserId = authentication.getName();
		Long adminUserIdLong = Long.parseLong(adminUserId);
		if (!userService.isAdmin(adminUserIdLong)) {
			return "redirect:/login";
		}
		try {
			AppointmentModel appointment = appointmentService.denyAppointment(appointmentId);
			UserModel admin = userService.getUserById(adminUserIdLong);
			auditLogService.logAction(admin.getUsername(),
					"Denied appointment for car ID: " + appointment.getAppointmentCar().getCarId(), "Appointment",
					appointmentId);
			notificationService.createNotification(appointment.getAppointmentUser().getUsername(),
					"Your test drive appointment has been denied.",
					NotificationModel.NotificationType.APPOINTMENT_STATUS);
			return "redirect:/admin/dashboard?appointmentDenied";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=AppointmentNotFound";
		}
	}

	// Shows the admin profile form within the dashboard
	@GetMapping("/admin/profile")
	public String showAdminProfileForm(Model model, Authentication authentication) {
		String userId = authentication.getName();
		Long userIdLong = Long.parseLong(userId);
		if (!userService.isAdmin(userIdLong)) {
			return "redirect:/login";
		}
		UserModel user = userService.getUserById(userIdLong);
		if (user == null) {
			return "redirect:/login";
		}
		model.addAttribute("user", user);
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("cars", carService.getAllCars());
		model.addAttribute("appointments", appointmentService.getAllAppointments());
		model.addAttribute("reports", reportService.getAllReports());
		model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(user.getUsername()));
		return "admin-dashboard";
	}

	// Updates the admin’s profile and logs the action
	@PostMapping("/admin/profile")
	public String updateAdminProfile(@Valid @ModelAttribute("user") UserModel user, BindingResult result, Model model,
			Authentication authentication) {
		String userId = authentication.getName();
		Long userIdLong = Long.parseLong(userId);
		if (!userService.isAdmin(userIdLong)) {
			return "redirect:/login";
		}
		UserModel loggedInUser = userService.getUserById(userIdLong);
		if (loggedInUser == null) {
			return "redirect:/login";
		}
		String username = loggedInUser.getUsername();
		if (result.hasErrors()) {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("cars", carService.getAllCars());
			model.addAttribute("appointments", appointmentService.getAllAppointments());
			model.addAttribute("reports", reportService.getAllReports());
			model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(username));
			return "admin-dashboard";
		}
		try {
			userService.updateUserProfile(user, username);
			auditLogService.logAction(username, "Updated admin profile", "User", user.getUserId());
			notificationService.createNotification(username, "Profile updated successfully",
					NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?profileUpdated";
		} catch (Exception e) {
			model.addAttribute("error", "Failed to update profile: " + e.getMessage());
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("cars", carService.getAllCars());
			model.addAttribute("appointments", appointmentService.getAllAppointments());
			model.addAttribute("reports", reportService.getAllReports());
			model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(username));
			return "admin-dashboard";
		}
	}

	// Resolves a user-submitted report and logs the action
	@PostMapping("/admin/reports/resolve")
	public String resolveReport(@RequestParam("reportId") Long reportId, Authentication authentication) {
		String userId = authentication.getName();
		Long userIdLong = Long.parseLong(userId);
		if (!userService.isAdmin(userIdLong)) {
			return "redirect:/login";
		}
		try {
			ReportModel report = reportService.resolveReport(reportId);
			UserModel admin = userService.getUserById(userIdLong);
			auditLogService.logAction(admin.getUsername(),
					"Resolved report for car ID: " + report.getReportCar().getCarId(), "Report", reportId);
			notificationService.createNotification(report.getReportUser().getUsername(),
					"Your report has been resolved.", NotificationModel.NotificationType.GENERAL);
			return "redirect:/admin/dashboard?reportResolved";
		} catch (Exception e) {
			return "redirect:/admin/dashboard?error=ReportNotFound";
		}
	}
}