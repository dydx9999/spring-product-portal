package com.example.demo.controller;

import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.demo.model.User;

@Controller
public class UserViewController {
    private final UserService userService;

    public UserViewController(UserService userService) {
        this.userService = userService;
    }

    // Display all user entries
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getUsers());
        return "users";
    }

    // Render user registration page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new User());
        return "register";
    }

    // Render Login Form Page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new User());
        return "login";
    }

    // Register Form Submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") User userForm) {
        userService.setUserDetails(userForm);
        return "redirect:/users";
    }

    // Login Form Submission is handled by Spring Security's form login filter (see
    // SecurityConfig)

    @GetMapping("/dashboard")
    public String renderDashboard(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("balance", currentUser.getBalance());
        return "dashboard";
    }

}
