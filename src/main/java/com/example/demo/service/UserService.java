package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    public User setUserDetails(User newUser) {
        newUser.setRole("USER");
        return saveUser(newUser);
    }

    public User setVendorDetails(User newVendor) {
        newVendor.setRole("VENDOR");
        return saveUser(newVendor);
    }

    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return user;
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public boolean validateLogin(User loginAttempt) {
        return repository.findByUsername(loginAttempt.getUsername())
                .map(user -> passwordEncoder.matches(loginAttempt.getPassword(), user.getPassword()))
                .orElse(false);
    }

    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

}
