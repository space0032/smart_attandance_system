package com.attendance.controller;

import com.attendance.model.Student;
import com.attendance.repository.AttendanceRepository;
import com.attendance.service.AttendanceService;
import com.attendance.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Controller for dashboard and UI pages
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;

    /**
     * Home page
     * 
     * @return Home page template
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Dashboard with attendance statistics
     * 
     * @param model Model
     * @return Dashboard template
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            LocalDate today = LocalDate.now();
            AttendanceService.AttendanceStats stats = attendanceService.getAttendanceStats(today);

            model.addAttribute("totalAttendance", stats.total());
            model.addAttribute("presentCount", stats.present());
            model.addAttribute("attendanceRate", String.format("%.1f%%", stats.getAttendanceRate()));
            model.addAttribute("currentDate", today);
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load dashboard data");
        }

        return "dashboard";
    }

    /**
     * Student management page
     * 
     * @param model Model
     * @return Students template
     */
    @GetMapping("/students")
    public String students(Model model) {
        try {
            model.addAttribute("students", studentService.getAllStudents());
        } catch (Exception e) {
            log.error("Error loading students", e);
            model.addAttribute("error", "Failed to load students");
        }

        return "students";
    }

    /**
     * Student details page
     * 
     * @param id    Student ID
     * @param model Model
     * @return Student details template
     */
    @GetMapping("/students/{id}")
    public String studentDetails(@PathVariable Long id, Model model) {
        try {
            Student student = studentService.getStudentById(id);
            log.info("Viewing Student: {}, FaceImagePath: {}", student.getStudentId(), student.getFaceImagePath());
            model.addAttribute("student", student);

            // Add attendance stats
            AttendanceService.AttendanceStats stats = attendanceService.getStudentStats(id);
            model.addAttribute("stats", stats);
            model.addAttribute("attendanceRate", String.format("%.1f", stats.getAttendanceRate()));
        } catch (Exception e) {
            log.error("Error loading student details", e);
            model.addAttribute("error", "Failed to load student details");
            return "redirect:/students";
        }

        return "student-details";
    }

    /**
     * Attendance view page
     * 
     * @return Attendance template
     */
    @GetMapping("/attendance")
    public String attendance() {
        return "attendance";
    }

    /**
     * Student registration page
     * 
     * @return Registration template
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * System setup guide
     * 
     * @return Setup guide template
     */
    @GetMapping("/setup-guide")
    public String setupGuide() {
        return "setup-guide";
    }
}
