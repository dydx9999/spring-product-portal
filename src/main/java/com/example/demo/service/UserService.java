package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.PageView;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

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

    public User setVendorDetails(User newUser) {
        newUser.setRole("VENDOR");
        return saveUser(newUser);
    }

    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return user;
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public PageView<User> getUsers(int page, int size) {
        return PageView.load(page, size, Sort.by(Sort.Direction.ASC, "id"), pageable -> repository.findAll(pageable));
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

    public User updateAccount(String currentUsername, String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        User user = getUserByUsername(currentUsername);
        repository.findByUsername(username)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
                });

        user.setUsername(username);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return repository.save(user);
    }

}
