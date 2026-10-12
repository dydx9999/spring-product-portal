package com.example.demo.controller;

import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.model.VendorFormDTO;

@Controller
public class UserViewController {
    private final UserService userService;
    private final VendorService vendorService;

    public UserViewController(UserService userService, VendorService vendorService) {
        this.userService = userService;
        this.vendorService = vendorService;
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

    @GetMapping("/register/vendor")
    public String showVendorRegistrationForm(Model model) {
        model.addAttribute("vendorForm", new VendorFormDTO());
        return "register-vendor";
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

    @PostMapping("/register/vendor")
    public String registerVendor(@ModelAttribute("vendorForm") VendorFormDTO vendorForm) {
        User newUser = new User();
        newUser.setUsername(vendorForm.getUsername());
        newUser.setPassword(vendorForm.getPassword());
        User savedUser = userService.setVendorDetails(newUser);
        vendorService.createVendor(savedUser.getId(), vendorForm.getBusinessName());
        return "redirect:/users";
    }

    // Login Form Submission is handled by Spring Security's form login filter (see
    // SecurityConfig)

    @GetMapping("/dashboard")
    public String renderDashboard(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("balance", currentUser.getBalance());
        model.addAttribute("role", currentUser.getRole());
        return "dashboard";
    }

    @GetMapping("/user/profile")
    public String displayProfile(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        String username = currentUser.getUsername();
        String role = currentUser.getRole();
        model.addAttribute("username", username);
        model.addAttribute("role", role);

        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VENDOR"));
        model.addAttribute("currentVendor",
                isVendor ? vendorService.getVendorByUserId(currentUser.getId()) : null);
        model.addAttribute("isVendor", isVendor);

        return "profile";
    }

    @GetMapping("/user/profile/update")
    public String showProfileUpdateForm(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VENDOR"));

        VendorFormDTO userForm = new VendorFormDTO();
        userForm.setUsername(currentUser.getUsername());
        model.addAttribute("userForm", userForm);
        model.addAttribute("isVendor", isVendor);

        if (isVendor) {
            Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
            userForm.setBusinessName(currentVendor.getBusinessName());
        }
        return "profile-update";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@ModelAttribute("userForm") VendorFormDTO userForm,
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VENDOR"));
        boolean credentialsChanged = !currentUser.getUsername().equals(userForm.getUsername())
                || (userForm.getPassword() != null && !userForm.getPassword().isBlank());

        User updatedUser = userService.updateAccount(currentUser.getUsername(), userForm.getUsername(),
                userForm.getPassword());
        if (isVendor) {
            vendorService.updateBusinessName(updatedUser.getId(), userForm.getBusinessName());
        }

        if (credentialsChanged) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?profileUpdated";
        }
        return "redirect:/user/profile";
    }

}
