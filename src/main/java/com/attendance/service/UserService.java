package com.attendance.service;

import com.attendance.model.User;
import com.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user with encrypted password
     */
    @Transactional
    public User createUser(String username, String password, User.Role role) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        log.info("Created new user: {} with role: {}", username, role);
        return savedUser;
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete user by ID
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
        log.info("Deleted user with ID: {}", id);
    }

    /**
     * Update user role
     */
    @Transactional
    public User updateUserRole(Long id, User.Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        log.info("Updated user {} role to: {}", user.getUsername(), newRole);
        return updatedUser;
    }

    /**
     * Check if user exists by username
     */
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
