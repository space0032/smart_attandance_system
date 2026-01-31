package com.attendance.repository;

import com.attendance.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Classroom entity
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    Optional<Classroom> findByCourseCode(String courseCode);
    
    boolean existsByCourseCode(String courseCode);
}
