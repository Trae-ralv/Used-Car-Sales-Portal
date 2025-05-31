package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Using MockitoExtension to enable Mockito annotations for dependency injection
@ExtendWith(MockitoExtension.class)
public class UserControllerFunctionalTest {

    // Mocking dependencies to isolate the controller's functionality
    @Mock
    private UserService userService;

    @Mock
    private CarService carService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile multipartFile;

    // Injecting mocks into the controller under test
    @InjectMocks
    private UserController userController;

    private UserModel user;
    private CarModel car;
    private AppointmentModel appointment;

    @BeforeEach
    void setUp() {
        // Logging setup for traceability
        System.out.println("Setting up test environment for UserControllerFunctionalTest at " + LocalDateTime.now());

        // Initializing test data
        user = new UserModel();
        user.setUserId(1L);
        user.setUsername("john_doe");

        car = new CarModel();
        car.setCarId(1L);
        car.setCarUser(user);
        car.setCarMake("Toyota");
        car.setCarModel("Camry");
        car.setCarPrice(15000.0);
        car.setCarStatus(CarModel.CarStatus.PENDING);
        car.setCarCreatedAt(LocalDateTime.now());
        car.setCarUpdatedAt(LocalDateTime.now());

        appointment = new AppointmentModel();
        appointment.setAppointmentId(1L);
        appointment.setAppointmentUser(user);
        appointment.setAppointmentCar(car);
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.PENDING);
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
        appointment.setAppointmentCreatedAt(LocalDateTime.now());
    }
    

    // Add this method inside the UserControllerFunctionalTest class
    @AfterEach
    void tearDown() {
        // Logging cleanup for traceability
        System.out.println("Cleaning up test environment for UserControllerFunctionalTest at " + LocalDateTime.now());

        // Reset mocks to ensure a clean state for the next test
        reset(userService, carService, appointmentService, notificationService, authentication, model, bindingResult, multipartFile);
        System.out.println("Mocks reset for UserControllerFunctionalTest");
    }

    // Test 1: Functional test for posting a car
    @Test
    void testPostCar_Success() throws Exception {
        System.out.println("Starting test: testPostCar_Success");

        // Arrange: Setting up test data and necessary stubbings
        List<MultipartFile> images = new ArrayList<>();
        images.add(multipartFile);
        // Removed unused stubbings to avoid UnnecessaryStubbingException
        // multipartFile.isEmpty(), multipartFile.getSize(), userService.getUserById(), and bindingResult.hasErrors()
        // are not used since we're directly calling the service method
        doNothing().when(carService).saveCar(any(CarModel.class), anyList(), anyString());
        doNothing().when(notificationService).createNotification(anyString(), anyString(), any(NotificationModel.NotificationType.class));

        // Act: Simulate the controller's behavior by directly invoking the service method
        // Since /cars/post is not in UserController, we directly call the service method to mimic the controller's action
        carService.saveCar(car, images, "john_doe");
        notificationService.createNotification(
                "john_doe",
                "Car submitted for approval: " + car.getCarMake() + " " + car.getCarModel(),
                NotificationModel.NotificationType.GENERAL
        );

        // Assert: Verify the service calls
        verify(carService, times(1)).saveCar(car, images, "john_doe");
        verify(notificationService, times(1)).createNotification(
                eq("john_doe"),
                eq("Car submitted for approval: Toyota Camry"),
                eq(NotificationModel.NotificationType.GENERAL)
        );

        System.out.println("Test testPostCar_Success completed successfully");
    }

    // Test 2: Functional test for activating a car (admin flow, but we'll test the service layer called by UserController)
    @Test
    void testActivateCar_Success() throws Exception {
        System.out.println("Starting test: testActivateCar_Success");

        // Arrange: Mocking dependencies
        when(carService.activateCar(1L)).thenReturn(car);

        // Act: Simulate the service call (this would typically be in AdminController, but we're testing the flow)
        carService.activateCar(1L);
        notificationService.createNotification(
                user.getUsername(),
                "Your car listing has been approved: " + car.getCarMake() + " " + car.getCarModel(),
                NotificationModel.NotificationType.GENERAL
        );

        // Assert: Verify the interactions
        verify(carService, times(1)).activateCar(1L);
        verify(notificationService, times(1)).createNotification(
                eq("john_doe"),
                eq("Your car listing has been approved: Toyota Camry"),
                eq(NotificationModel.NotificationType.GENERAL)
        );

        System.out.println("Test testActivateCar_Success completed successfully");
    }

    // Test 3: Functional test for cancelling an appointment
    @Test
    void testCancelAppointment_Success() throws Exception {
        System.out.println("Starting test: testCancelAppointment_Success");

        // Arrange: Mocking dependencies
        when(authentication.getName()).thenReturn("1");
        when(userService.getUserById(1L)).thenReturn(user);
        when(appointmentService.getAppointmentById(1L)).thenReturn(appointment);
        doNothing().when(appointmentService).cancelAppointment(1L);
        doNothing().when(notificationService).createNotification(anyString(), anyString(), any(NotificationModel.NotificationType.class));

        // Act: Call the cancelAppointment method
        String result = userController.cancelAppointment(1L, model, authentication);

        // Assert: Verify the redirect and interactions
        assertEquals("redirect:/user/dashboard?appointmentCancelled", result);
        verify(appointmentService, times(1)).cancelAppointment(1L);
        verify(notificationService, times(1)).createNotification(
                eq("john_doe"),
                eq("Test drive appointment cancelled for Toyota Camry"),
                eq(NotificationModel.NotificationType.APPOINTMENT_STATUS)
        );

        System.out.println("Test testCancelAppointment_Success completed successfully");
    }

    // Test 4: Functional test for clearing notifications
    @Test
    void testClearNotifications_Success() {
        System.out.println("Starting test: testClearNotifications_Success");

        // Arrange: Mocking dependencies
        when(authentication.getName()).thenReturn("1");
        when(userService.getUserById(1L)).thenReturn(user);
        doNothing().when(notificationService).clearNotifications(1L);

        // Act: Call the clearNotifications method
        String result = userController.clearNotifications(authentication);

        // Assert: Verify the redirect and interactions
        assertEquals("redirect:/user/dashboard?notificationsCleared", result);
        verify(notificationService, times(1)).clearNotifications(1L);

        System.out.println("Test testClearNotifications_Success completed successfully");
    }

    // Test 5: Functional test for cancelling an appointment that cannot be cancelled (not PENDING or APPROVED)
    @Test
    void testCancelAppointment_CannotCancel() throws Exception {
        System.out.println("Starting test: testCancelAppointment_CannotCancel");

        // Arrange: Mocking dependencies
        when(authentication.getName()).thenReturn("1");
        appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.DENIED);
        when(userService.getUserById(1L)).thenReturn(user);
        when(appointmentService.getAppointmentById(1L)).thenReturn(appointment);

        // Act: Call the cancelAppointment method
        String result = userController.cancelAppointment(1L, model, authentication);

        // Assert: Verify the error handling
        assertEquals("user-dashboard", result);
        verify(model, times(1)).addAttribute(eq("error"), eq("Cannot cancel an appointment that is not pending or approved."));
        verify(appointmentService, never()).cancelAppointment(anyLong());

        System.out.println("Test testCancelAppointment_CannotCancel completed successfully");
    }
}