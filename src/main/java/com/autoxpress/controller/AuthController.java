package com.autoxpress.controller;

import com.autoxpress.model.UserModel;
import com.autoxpress.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Displays the login page
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    // Displays the registration form with a new UserModel instance
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserModel());
        return "register";
    }

    // Handles user registration, validates the form, and redirects to login on success
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserModel user,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(user);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Displays the password reset request form
    @GetMapping("/password-reset-request")
    public String showPasswordResetRequestForm(Model model) {
        return "password-reset-request";
    }

    // Processes password reset requests by email, displays success or error
    @PostMapping("/password-reset-request")
    public String requestPasswordReset(
            @RequestParam("email") String email,
            Model model) {
        try {
            userService.requestPasswordReset(email);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while processing your request.");
        }
        return "password-reset-request";
    }

    // Displays the password reset form for a given token
    @GetMapping("/password-reset")
    public String showPasswordResetForm(
            @RequestParam("token") String token,
            Model model) {
        model.addAttribute("user", new UserModel());
        return "password-reset";
    }

    // Handles password reset, validates the form, checks password match, and updates the password
    @PostMapping("/password-reset")
    public String resetPassword(
            @RequestParam("token") String token,
            @Valid @ModelAttribute("user") UserModel user,
            @RequestParam("confirmPassword") String confirmPassword,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "password-reset";
        }
        if (!user.getUserPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "password-reset";
        }
        try {
            userService.resetPassword(token, user.getUserPassword());
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "password-reset";
    }
}