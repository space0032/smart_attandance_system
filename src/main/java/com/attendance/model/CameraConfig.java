package com.attendance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to store CCTV camera configuration for a classroom
 */
@Entity
@Table(name = "camera_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false, unique = true)
    private Classroom classroom;

    @NotBlank(message = "RTSP URL is required")
    @Column(name = "rtsp_url", nullable = false)
    private String rtspUrl;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "capture_interval_seconds")
    private int captureIntervalSeconds = 5;
}
