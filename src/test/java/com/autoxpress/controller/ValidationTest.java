package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
public class ValidationTest {

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
        System.out.println("Setting up test environment for ValidationTest at " + LocalDateTime.now());

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

        // Mocking UserService to return the test user
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.getUserByUsername("john_doe")).thenReturn(user);
    }

    // Test 1: Validate car posting with invalid images (too many images)
    @Test
    void testCarPost_InvalidImages_TooMany() throws Exception {
        System.out.println("Starting test: testCarPost_InvalidImages_TooMany");

        // Arrange: Create a valid car with too many images (more than 5)
        MockMultipartFile[] images = new MockMultipartFile[6];
        for (int i = 0; i < 6; i++) {
            images[i] = new MockMultipartFile(
                    "images",
                    "test-image-" + i + ".jpg",
                    "image/jpeg",
                    ("test image content " + i).getBytes()
            );
        }

        // Act: Submit the car posting form with too many images, including CSRF token
        mockMvc.perform(MockMvcRequestBuilders.multipart("/cars/post")
                .file(images[0])
                .file(images[1])
                .file(images[2])
                .file(images[3])
                .file(images[4])
                .file(images[5])
                .param("car.carMake", "Toyota")
                .param("car.carModel", "Camry")
                .param("car.carPrice", "15000.0")
                .with(SecurityMockMvcRequestPostProcessors.user("1").authorities(new SimpleGrantedAuthority("ROLE_USER"))) // Explicitly set authentication
                .with(SecurityMockMvcRequestPostProcessors.csrf()) // Add CSRF token
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                // Assert: Verify the response (should return to the form with errors)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("post-car"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                .andExpect(MockMvcResultMatchers.model().attribute("error", "Maximum 5 images allowed."));

        System.out.println("Test testCarPost_InvalidImages_TooMany completed successfully");
    }

    
    // Test 2: Validate car posting with valid data
    @Test
    void testCarPost_ValidData() throws Exception {
        System.out.println("Starting test: testCarPost_ValidData");

        // Arrange: Create a valid car with one image
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Mock the service calls to simulate successful posting
        doNothing().when(carService).saveCar(any(CarModel.class), anyList(), anyString());
        doNothing().when(notificationService).createNotification(anyString(), anyString(), any(NotificationModel.NotificationType.class));
        when(notificationService.getUnreadNotificationCount("john_doe")).thenReturn(0L);

        // Act: Submit the car posting form with valid data, including CSRF token
        mockMvc.perform(MockMvcRequestBuilders.multipart("/cars/post")
                .file(image)
                .param("car.carMake", "Toyota")
                .param("car.carModel", "Camry")
                .param("car.carPrice", "15000.0")
                .with(SecurityMockMvcRequestPostProcessors.user("1").authorities(new SimpleGrantedAuthority("ROLE_USER"))) // Explicitly set authentication
                .with(SecurityMockMvcRequestPostProcessors.csrf()) // Add CSRF token
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                // Assert: Verify the response (should redirect to dashboard with success)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/user/dashboard?submitted"));

        System.out.println("Test testCarPost_ValidData completed successfully");
    }
}