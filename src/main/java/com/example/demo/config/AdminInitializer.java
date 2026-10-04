package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    ApplicationRunner seedAdmin(UserRepository repository, PasswordEncoder encoder,
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password) {
        return args -> {
            if (repository.findByUsername(username).isEmpty()) {
                User admin = new User();
                admin.setUsername(username);
                admin.setPassword(encoder.encode(password));
                admin.setRole("ADMIN");
                repository.save(admin);
            }
        };
    }
}
