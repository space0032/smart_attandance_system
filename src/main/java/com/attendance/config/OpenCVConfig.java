package com.attendance.config;

import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenCV library
 */
@Slf4j
@Configuration
public class OpenCVConfig {

    /**
     * Load OpenCV native library
     * 
     * @return Configuration bean
     */
    @Bean
    public String loadOpenCV() {
        try {
            OpenCV.loadLocally();
            log.info("OpenCV library loaded successfully");
            return "OpenCV loaded";
        } catch (Exception e) {
            log.error("Failed to load OpenCV library", e);
            throw new RuntimeException("Failed to initialize OpenCV", e);
        }
    }
}
