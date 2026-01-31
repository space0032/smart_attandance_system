package com.attendance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Attendance entity representing attendance records
 */
@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "classroom_id",
        "attendance_date" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    @NotNull(message = "Classroom is required")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Classroom classroom;

    @Column(name = "attendance_date", nullable = false)
    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @Column(name = "check_in_time", nullable = false)
    @NotNull(message = "Check-in time is required")
    private LocalTime checkInTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "detection_count")
    private int detectionCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
        if (checkInTime == null) {
            checkInTime = LocalTime.now();
        }
    }

    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LATE
    }
}
