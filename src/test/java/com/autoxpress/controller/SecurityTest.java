package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Using SpringExtension to enable Spring Boot test features
@ExtendWith(SpringExtension.class)
// Using WebMvcTest to test controllers in isolation with MockMvc
@WebMvcTest(controllers = {CarController.class})
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CarService carService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private NotificationService notificationService;

    // Added mock for ReportService to satisfy CarController dependency
    @MockBean
    private ReportService reportService;

    private UserModel user;
    private CarModel car;

    @BeforeEach
    void setUp() {
        // Logging setup for traceability
        System.out.println("Setting up test environment for SecurityTest at " + LocalDateTime.now());

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

        // Mocking UserService to return the test user (though not used in unauthenticated test)
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.getUserByUsername("john_doe")).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        // Logging cleanup for traceability
        System.out.println("Cleaning up test environment for SecurityTest at " + LocalDateTime.now());

        // Reset mocks to ensure a clean state for the next test
        reset(userService, carService, appointmentService, notificationService, reportService);
        System.out.println("Mocks reset for SecurityTest");
    }

    // Test 1: Verify that unauthenticated users are denied access to /cars/post
    @Test
    void testUnauthenticatedUserAccess_Denied() throws Exception {
        System.out.println("Starting test: testUnauthenticatedUserAccess_Denied");

        // Arrange: Create a valid car posting request with one image
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act: Submit the car posting form as an unauthenticated user (no user set)
        mockMvc.perform(MockMvcRequestBuilders.multipart("/cars/post")
                .file(image)
                .param("car.carMake", "Toyota")
                .param("car.carModel", "Camry")
                .param("car.carPrice", "15000.0")
                .with(SecurityMockMvcRequestPostProcessors.csrf()) // Add CSRF token
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                // Assert: Verify the response (should return 401 Unauthorized for unauthenticated user)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        System.out.println("Test testUnauthenticatedUserAccess_Denied completed successfully");
    }
}