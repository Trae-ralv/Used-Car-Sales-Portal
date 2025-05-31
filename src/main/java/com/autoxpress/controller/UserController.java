package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationService notificationService;

    // Displays the user dashboard with paginated data for cars, appointments, and notifications
    @GetMapping("/user/dashboard")
    public String userDashboard(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String submitted,
            @RequestParam(defaultValue = "0") int pagePostedCars,
            @RequestParam(defaultValue = "0") int pageAppointments,
            @RequestParam(defaultValue = "0") int pageNotifications) {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel user = userService.getUserById(userIdLong);
        if (user == null) {
            return "redirect:/login";
        }

        int pageSize = 10; // Limit of 10 items per page

        // Fetch paginated data
        Pageable postedCarsPageable = PageRequest.of(pagePostedCars, pageSize);
        Page<CarModel> postedCarsPage = carService.getCarsByUser(userIdLong, postedCarsPageable);
        model.addAttribute("postedCarsPage", postedCarsPage);
        model.addAttribute("postedCars", postedCarsPage.getContent());

        Pageable appointmentsPageable = PageRequest.of(pageAppointments, pageSize);
        Page<AppointmentModel> appointmentsPage = appointmentService.getAppointmentsByUser(userIdLong, appointmentsPageable);
        model.addAttribute("appointmentsPage", appointmentsPage);
        model.addAttribute("appointments", appointmentsPage.getContent());

        Pageable notificationsPageable = PageRequest.of(pageNotifications, pageSize);
        Page<NotificationModel> notificationsPage = notificationService.getNotificationsByUser(userIdLong, notificationsPageable);
        model.addAttribute("notificationsPage", notificationsPage);
        model.addAttribute("notifications", notificationsPage.getContent());

        model.addAttribute("user", user);
        model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(user.getUsername()));
        if (submitted != null) {
            model.addAttribute("message", "Car submitted for approval!");
        }
        return "user-dashboard";
    }

    // Shows the profile form for editing user details
    @GetMapping("/user/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel user = userService.getUserById(userIdLong);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(user.getUsername()));
        return "user-dashboard";
    }

    // Handles profile updates with form validation
    @PostMapping("/user/profile")
    public String updateProfile(
            @Valid @ModelAttribute("user") UserModel user,
            BindingResult result,
            Model model,
            Authentication authentication) throws Exception {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel loggedInUser = userService.getUserById(userIdLong);
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        String username = loggedInUser.getUsername();
        if (result.hasErrors()) {
            model.addAttribute("postedCars", carService.getCarsByUser(userIdLong));
            model.addAttribute("appointments", appointmentService.getAppointmentsByUser(userIdLong));
            model.addAttribute("notifications", notificationService.getNotificationsByUser(userIdLong));
            model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(username));
            model.addAttribute("user", loggedInUser);
            return "user-dashboard";
        }
        userService.updateUserProfile(user, username);
        notificationService.createNotification(
                username,
                "Profile updated successfully",
                com.autoxpress.model.NotificationModel.NotificationType.GENERAL
        );
        return "redirect:/user/dashboard?updated";
    }

    // Cancels a user’s appointment if it’s pending or approved
    @PostMapping("/user/appointments/cancel")
    public String cancelAppointment(@RequestParam("appointmentId") Long appointmentId, Model model, Authentication authentication) {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel user = userService.getUserById(userIdLong);
        if (user == null) {
            return "redirect:/login";
        }
        String username = user.getUsername();
        AppointmentModel appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null || !appointment.getAppointmentUser().getUserId().equals(userIdLong)) {
            model.addAttribute("error", "Failed to cancel appointment: Invalid appointment or permission.");
            model.addAttribute("user", user);
            model.addAttribute("postedCars", carService.getCarsByUser(userIdLong));
            model.addAttribute("appointments", appointmentService.getAppointmentsByUser(userIdLong));
            model.addAttribute("notifications", notificationService.getNotificationsByUser(userIdLong));
            model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(username));
            return "user-dashboard";
        }
        if (appointment.getAppointmentStatus() != AppointmentModel.AppointmentStatus.PENDING && 
            appointment.getAppointmentStatus() != AppointmentModel.AppointmentStatus.APPROVED) {
            model.addAttribute("error", "Cannot cancel an appointment that is not pending or approved.");
            model.addAttribute("user", user);
            model.addAttribute("postedCars", carService.getCarsByUser(userIdLong));
            model.addAttribute("appointments", appointmentService.getAppointmentsByUser(userIdLong));
            model.addAttribute("notifications", notificationService.getNotificationsByUser(userIdLong));
            model.addAttribute("notificationCount", notificationService.getUnreadNotificationCount(username));
            return "user-dashboard";
        }
        appointmentService.cancelAppointment(appointmentId);
        notificationService.createNotification(
                username,
                "Test drive appointment cancelled for " + appointment.getAppointmentCar().getCarMake() + " " + appointment.getAppointmentCar().getCarModel(),
                NotificationModel.NotificationType.APPOINTMENT_STATUS
        );
        return "redirect:/user/dashboard?appointmentCancelled";
    }
    
    // Clears all notifications for the logged-in user
    @PostMapping("/user/notifications/clear")
    public String clearNotifications(Authentication authentication) {
        String userId = authentication.getName();
        Long userIdLong = Long.parseLong(userId);
        UserModel user = userService.getUserById(userIdLong);
        if (user == null) {
            return "redirect:/login";
        }
        notificationService.clearNotifications(userIdLong);
        return "redirect:/user/dashboard?notificationsCleared";
    }
}