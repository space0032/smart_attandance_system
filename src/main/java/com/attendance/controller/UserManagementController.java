package com.attendance.controller;

import com.attendance.model.User;
import com.attendance.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for user management operations (ADMIN only)
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    /**
     * Display user management page
     */
    @GetMapping
    public String getUsersPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    /**
     * Create new user
     */
    @PostMapping("/create")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam User.Role role,
            RedirectAttributes redirectAttributes) {

        try {
            userService.createUser(username, password, role);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + username + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating user", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Delete user
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Update user role
     */
    @PostMapping("/update-role/{id}")
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam User.Role role,
            RedirectAttributes redirectAttributes) {

        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "User role updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating user role", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update user role: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}
