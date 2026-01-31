package com.attendance.repository;

import com.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByAttendanceDateAndClassroomId(LocalDate date, Long classroomId);
    
    List<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);
    
    Optional<Attendance> findByStudentIdAndClassroomIdAndAttendanceDate(
            Long studentId, Long classroomId, LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date")
    long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'PRESENT'")
    long countPresentByDate(@Param("date") LocalDate date);
}
