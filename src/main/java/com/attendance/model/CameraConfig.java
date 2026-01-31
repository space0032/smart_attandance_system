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

    @Column(name = "lecture_duration_minutes")
    private int lectureDurationMinutes = 60;

    @Column(name = "snapshots_per_lecture")
    private int snapshotsPerLecture = 4;

    /**
     * Calculate interval in seconds dynamically
     */
    public long getCalculatedIntervalSeconds() {
        if (snapshotsPerLecture <= 0)
            return 300; // Default fallback
        return (long) (lectureDurationMinutes * 60) / snapshotsPerLecture;
    }
}
