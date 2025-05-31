package com.autoxpress.controller;

import com.autoxpress.model.UserModel;
import com.autoxpress.repository.UserRepository;
import com.autoxpress.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Using SpringExtension to enable Spring Boot test features
@ExtendWith(SpringExtension.class)
// Using SpringBootTest to load the full application context for database integration
@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@MockBean
	private CarService carService;

	@MockBean
	private AppointmentService appointmentService;

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private ReportService reportService;

	private UserModel user;

	@BeforeEach
	void setUp() {
		// Logging setup for traceability
		System.out.println("Setting up test environment for RoleBasedTest at " + LocalDateTime.now());

		// Initializing test data
		user = new UserModel();
		user.setUserId(1L);
		user.setUsername("john_doe");
		user.setEmail("john.doe@example.com");
		user.setUserPassword("password123");
		user.setUserRole(UserModel.Role.USER);
		user.setUserCreatedAt(LocalDateTime.now());
		user.setUserUpdatedAt(LocalDateTime.now());

		// Mocking NotificationService to return notification count (not used in this
		// test)
		when(notificationService.getUnreadNotificationCount(anyString())).thenReturn(0L);

		// Save the user to the database for authentication
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String createdAt = LocalDateTime.now().format(formatter);
		String updatedAt = LocalDateTime.now().format(formatter);

		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/register").param("username", "john_doe")
					.param("email", "john.doe@example.com").param("userPassword", "password123")
					.param("userRole", "USER").param("userCreatedAt", createdAt).param("userUpdatedAt", updatedAt)
					.with(SecurityMockMvcRequestPostProcessors.csrf()))
					.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
					.andExpect(MockMvcResultMatchers.redirectedUrl("/login?success"));
		} catch (Exception e) {
			System.out.println("Failed to register user in setup: " + e.getMessage());
		}
	}

	@AfterEach
	void tearDown() {
		// Logging cleanup for traceability
		System.out.println("Cleaning up test data for RoleBasedTest at " + LocalDateTime.now());

		// Delete the test user created during the test
		UserModel testUser = userRepository.findByUsername("john_doe").orElse(null);
		if (testUser != null) {
			userRepository.delete(testUser);
			System.out.println("Deleted test user: john_doe");
		}
	}

	// Test 1: Verify that a USER role user is redirected when accessing admin
	// dashboard
	@Test
	@WithMockUser(username = "1", roles = { "USER" }) // Use userId as username, as per userDetailsService
	void testUserRoleAccessToAdminDashboard_Redirects() throws Exception {
		System.out.println("Starting test: testUserRoleAccessToAdminDashboard_Redirects");

		// Act: Attempt to access the admin dashboard as a USER role user
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/admin/dashboard"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl(
						"/user/dashboard?error=Access%20denied.%20You%20do%20not%20have%20permission%20to%20access%20this%20page."))
				.andReturn();

		// Debug: Inspect the response
		System.out.println("Response status: " + result.getResponse().getStatus());
		System.out.println("Response content: " + result.getResponse().getContentAsString());

		// Verify that the user exists in the database (from setup)
		UserModel savedUser = userRepository.findByUsername("john_doe").orElse(null);
		assertNotNull(savedUser, "User should exist in the database");
		assertEquals("john_doe", savedUser.getUsername(), "Username should match");
		assertEquals("john.doe@example.com", savedUser.getEmail(), "Email should match");

		System.out.println("Test testUserRoleAccessToAdminDashboard_Redirects completed successfully");
	}
}