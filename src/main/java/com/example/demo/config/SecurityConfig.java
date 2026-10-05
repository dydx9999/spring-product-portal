package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.repository.UserRepository;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**",
                                                "/register/**",
                                                "/cart/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/users",
                                                                "/register/**",
                                                                "/api/**",
                                                                "/dashboard/**",
                                                                "/css/**",
                                                                "/webjars/**",
                                                                "/products/**",
                                                                "/add-product/**",
                                                                "/add-product/**",
                                                                "/products/product-search",
                                                                "/cart",
                                                                "/vendor-dashboard/**",
                                                                "/vendor/**",
                                                                "/home/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/products", true)
                                                .failureUrl("/login?error")
                                                .permitAll());
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository) {
                return username -> userRepository.findByUsername(username)
                                .map(user -> org.springframework.security.core.userdetails.User
                                                .withUsername(user.getUsername())
                                                .password(user.getPassword())
                                                .roles(user.getRole())
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
}
