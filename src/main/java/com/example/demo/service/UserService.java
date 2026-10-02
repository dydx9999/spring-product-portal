package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final Random rand = new Random();
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    public User setUserDetails(User newUser) {
        String requestUsername = newUser.getUsername();
        String requestPassword = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(requestPassword));
        newUser.setUsername(requestUsername);
        repository.save(newUser);
        return newUser;
    }

    public User createNewCard(User user) {
        List<String> cards = user.getCards();
        if (cards == null) {
            cards = new ArrayList<>();
        }
        String cardNumber = String.format("%014d", rand.nextInt(1000000));
        cards.add(cardNumber);
        user.setCards(cards);
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
