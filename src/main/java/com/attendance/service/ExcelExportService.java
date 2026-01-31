package com.attendance.service;

import com.attendance.model.Attendance;
import com.attendance.util.ExcelGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for Excel export operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final AttendanceService attendanceService;
    private final ExcelGenerator excelGenerator;

    /**
     * Export attendance to Excel file
     * 
     * @param classroomId Classroom ID
     * @param date Attendance date
     * @return ByteArrayInputStream containing Excel file
     * @throws IOException if export fails
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportAttendanceToExcel(Long classroomId, LocalDate date) 
            throws IOException {
        
        // Get attendance records
        List<Attendance> attendanceList = attendanceService
                .getAttendanceByDateAndClassroom(date, classroomId);

        if (attendanceList.isEmpty()) {
            log.warn("No attendance records found for classroom {} on {}", classroomId, date);
        }

        // Generate Excel file
        ByteArrayInputStream excelFile = excelGenerator.generateAttendanceExcel(attendanceList, date);
        
        log.info("Exported {} attendance records to Excel for date {}", 
                attendanceList.size(), date);
        
        return excelFile;
    }

    /**
     * Generate filename for Excel export
     * 
     * @param date Attendance date
     * @return Filename
     */
    public String getFilename(LocalDate date) {
        return excelGenerator.generateFilename(date);
    }
}
