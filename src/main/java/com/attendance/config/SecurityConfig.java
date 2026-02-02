package com.attendance.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final com.attendance.service.CustomUserDetailsService userDetailsService;

        @Bean
        public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
                org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                authProvider.setHideUserNotFoundExceptions(false); // Enable detailed errors
                return authProvider;
        }

        /**
         * Configure HTTP security
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authenticationProvider(authenticationProvider()) // Use custom provider
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/css/**", "/js/**", "/uploads/**").permitAll()
                                                .requestMatchers("/", "/login", "/register").permitAll()
                                                .requestMatchers("/settings/**").hasRole("ADMIN")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/camera/**") // Keep camera stream open if
                                                                                           // needed, but secure others
                                )
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
