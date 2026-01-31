package com.attendance.repository;

import com.attendance.model.CameraConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CameraConfigRepository extends JpaRepository<CameraConfig, Long> {
    Optional<CameraConfig> findByClassroomId(Long classroomId);
}
