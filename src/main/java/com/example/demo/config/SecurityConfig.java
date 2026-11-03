package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.repository.UserRepository;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/assets/**")
                                .addResourceLocations("classpath:/assets/");
        }

        @Bean
        public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
                JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
                repository.setDataSource(dataSource);
                return repository;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        PersistentTokenRepository tokenRepository,
                        @Value("${app.remember-me.key}") String rememberMeKey) throws Exception {
                http
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**",
                                                "/register/**",
                                                "/cart/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/products/wishlist/**").hasRole("USER")
                                                .requestMatchers("/api/products/vendor-pick-list/**").hasRole("VENDOR")
                                                .requestMatchers("/users",
                                                                "/register/**",
                                                                "/api/**",
                                                                "/dashboard/**",
                                                                "/assets/**",
                                                                "/css/**",
                                                                "/webjars/**",
                                                                "/products/**",
                                                                "/product-add/**",
                                                                "/product-add/**",
                                                                "/products/product-search",
                                                                "/vendor-dashboard/**",
                                                                "/home/**")
                                                .permitAll()
                                                .requestMatchers("/orders/**").authenticated()
                                                .requestMatchers("/user/**").authenticated()
                                                .requestMatchers("/vendor/profile/**").authenticated()
                                                .requestMatchers("/vendor/dashboard/**").hasRole("VENDOR")
                                                .requestMatchers("/cart/**").hasRole("USER")

                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler((request, response, authentication) -> {
                                                        boolean vendor = authentication.getAuthorities().stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_VENDOR"));
                                                        response.sendRedirect(request.getContextPath()
                                                                        + (vendor ? "/vendor" : "/products"));
                                                })
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .rememberMe(remember -> remember
                                                .key(rememberMeKey)
                                                .tokenRepository(tokenRepository)
                                                .tokenValiditySeconds(60 * 60 * 24 * 30))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "remember-me"));
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
                                                .authorities(new SimpleGrantedAuthority(
                                                                "ROLE_" + user.getRole()))
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
}
