package com.autoxpress.controller;

import com.autoxpress.model.*;
import com.autoxpress.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

// Using SpringExtension to enable Spring Boot test features
@ExtendWith(SpringExtension.class)
// Using WebMvcTest to test the controller in isolation with MockMvc
@WebMvcTest(UserController.class)
// Simulating an authenticated user with the correct username matching the user ID
@WithMockUser(username = "1", roles = { "USER" })
public class UserDashboardUITest {

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

	private UserModel user;
	private CarModel car;
	private AppointmentModel appointment;

	@BeforeEach
	void setUp() {
		// Logging setup for traceability
		System.out.println("Setting up test environment for UserDashboardUITest at " + LocalDateTime.now());

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
		car.setCarStatus(CarModel.CarStatus.ACTIVE);
		car.setCarCreatedAt(LocalDateTime.now());
		car.setCarUpdatedAt(LocalDateTime.now());

		// Create a list of 16 cars to ensure pagination (page size is 15)
		List<CarModel> cars = IntStream.range(0, 16).mapToObj(i -> {
			CarModel c = new CarModel();
			c.setCarId((long) i + 1);
			c.setCarUser(user);
			c.setCarMake("Toyota");
			c.setCarModel("Camry " + i);
			c.setCarPrice(15000.0 + i * 1000);
			c.setCarStatus(CarModel.CarStatus.ACTIVE);
			c.setCarCreatedAt(LocalDateTime.now());
			c.setCarUpdatedAt(LocalDateTime.now());
			return c;
		}).collect(Collectors.toList());

		appointment = new AppointmentModel();
		appointment.setAppointmentId(1L);
		appointment.setAppointmentUser(user);
		appointment.setAppointmentCar(car);
		appointment.setAppointmentStatus(AppointmentModel.AppointmentStatus.PENDING);
		appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
		appointment.setAppointmentCreatedAt(LocalDateTime.now());

		// Mocking UserService to return the test user
		when(userService.getUserById(1L)).thenReturn(user);
		when(userService.getUserByUsername("john_doe")).thenReturn(user);

		// Mocking CarService to return a list of 16 cars to trigger pagination
		Pageable pageable = PageRequest.of(0, 15);
		Page<CarModel> carPage = new PageImpl<>(cars, pageable, cars.size());
		when(carService.getCarsByUser(eq(1L), any(Pageable.class))).thenReturn(carPage);
		// Debug: Log the mocked Page to confirm it's set up correctly
		System.out.println("Mocked carPage content size: " + carPage.getContent().size() + ", total pages: "
				+ carPage.getTotalPages());

		// Mocking AppointmentService to return a list with one PENDING appointment
		Page<AppointmentModel> appointmentPage = new PageImpl<>(Arrays.asList(appointment), pageable, 1);
		when(appointmentService.getAppointmentsByUser(eq(1L), any(Pageable.class))).thenReturn(appointmentPage);
		System.out.println("Mocked appointmentPage content: " + appointmentPage.getContent());

		// Mocking NotificationService to return an empty list of notifications (to
		// simplify the test)
		Page<NotificationModel> emptyNotificationPage = new PageImpl<>(Collections.emptyList());
		when(notificationService.getNotificationsByUser(anyLong(), any(Pageable.class)))
				.thenReturn(emptyNotificationPage);
		when(notificationService.getUnreadNotificationCount(anyString())).thenReturn(0L);
	}

	// Test 1: Verify that the user dashboard renders correctly
	@Test
	void testUserDashboard_RendersCorrectly() throws Exception {
		System.out.println("Starting test: testUserDashboard_RendersCorrectly");

		// Act: Make a GET request to /user/dashboard
		mockMvc.perform(MockMvcRequestBuilders.get("/user/dashboard"))
				// Assert: Verify the response and rendered content
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("<h2>User Dashboard</h2>")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("id=\"profile-panel\"")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("id=\"posted-cars-panel\"")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("id=\"appointments-panel\"")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("id=\"notifications-panel\"")));

		System.out.println("Test testUserDashboard_RendersCorrectly completed successfully");
	}

	// Test 2: Verify pagination links are present in the Posted Cars panel
	@Test
	void testUserDashboard_PaginationLinksPresent() throws Exception {
		System.out.println("Starting test: testUserDashboard_PaginationLinksPresent");

		// Act: Make a GET request to /user/dashboard
		mockMvc.perform(MockMvcRequestBuilders.get("/user/dashboard"))
				// Assert: Verify pagination links in the Posted Cars panel
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("Posted Cars Pagination")))
				.andExpect(
						MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("pagePostedCars")));

		System.out.println("Test testUserDashboard_PaginationLinksPresent completed successfully");
	}

	// Test 3: Verify the Clear Notifications button and modal are present
	@Test
	void testUserDashboard_ClearNotificationsButtonAndModal() throws Exception {
		System.out.println("Starting test: testUserDashboard_ClearNotificationsButtonAndModal");

		// Act: Make a GET request to /user/dashboard
		mockMvc.perform(MockMvcRequestBuilders.get("/user/dashboard"))
				// Assert: Verify the Clear Notifications button and modal
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("data-bs-target=\"#clearNotificationsModal\"")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("id=\"clearNotificationsModal\"")))
				// Check for the rendered action attribute instead of Thymeleaf syntax
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("action=\"/user/notifications/clear\"")));

		System.out.println("Test testUserDashboard_ClearNotificationsButtonAndModal completed successfully");
	}

	// Test 4: Verify the form for cancelling an appointment is present
	@Test
	void testUserDashboard_CancelAppointmentForm() throws Exception {
		System.out.println("Starting test: testUserDashboard_CancelAppointmentForm");

		// Act: Make a GET request to /user/dashboard
		mockMvc.perform(MockMvcRequestBuilders.get("/user/dashboard"))
				// Assert: Verify the Cancel Appointment form
				.andExpect(MockMvcResultMatchers.status().isOk())
				// Check for the rendered action attribute instead of Thymeleaf syntax
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("action=\"/user/appointments/cancel\"")))
				.andExpect(MockMvcResultMatchers.content()
						.string(org.hamcrest.Matchers.containsString("name=\"appointmentId\"")));

		System.out.println("Test testUserDashboard_CancelAppointmentForm completed successfully");
	}
}