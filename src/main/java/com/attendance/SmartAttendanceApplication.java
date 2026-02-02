package com.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Smart Attendance System
 * 
 * @author Smart Attendance Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SmartAttendanceApplication {

    public static void main(String[] args) {
        // Load OpenCV native libraries
        nu.pattern.OpenCV.loadLocally();
        SpringApplication.run(SmartAttendanceApplication.class, args);
    }
}
