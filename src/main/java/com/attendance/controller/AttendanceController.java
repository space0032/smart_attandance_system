package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.model.Attendance;
import com.attendance.service.AttendanceService;
import com.attendance.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for attendance management
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;

    /**
     * Process camera image and mark attendance
     * 
     * @param imageFile Camera image file
     * @param classroomId Classroom ID
     * @return API response with marked attendance records
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<List<Attendance>>> processCameraFeed(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("classroomId") Long classroomId) {
        
        try {
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Image file is required"));
            }

            List<Attendance> attendanceRecords = attendanceService
                    .processCameraFeed(imageFile, classroomId);
            
            String message = String.format("Processed camera feed, marked attendance for %d student(s)", 
                    attendanceRecords.size());
            
            return ResponseEntity.ok(ApiResponse.success(message, attendanceRecords));
        } catch (IllegalArgumentException e) {
            log.error("Validation error during camera feed processing", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing camera feed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process camera feed: " + e.getMessage()));
        }
    }

    /**
     * Export attendance to Excel
     * 
     * @param classroomId Classroom ID
     * @param date Attendance date
     * @return Excel file download
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAttendance(
            @RequestParam("classroomId") Long classroomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            ByteArrayInputStream excelFile = excelExportService
                    .exportAttendanceToExcel(classroomId, date);
            
            String filename = excelExportService.getFilename(date);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(excelFile));
        } catch (Exception e) {
            log.error("Error exporting attendance to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get attendance list by date and classroom
     * 
     * @param classroomId Classroom ID
     * @param date Attendance date
     * @return API response with attendance list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceList(
            @RequestParam("classroomId") Long classroomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            List<Attendance> attendanceList = attendanceService
                    .getAttendanceByDateAndClassroom(date, classroomId);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Attendance records retrieved successfully", attendanceList));
        } catch (Exception e) {
            log.error("Error retrieving attendance list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance list"));
        }
    }

    /**
     * Get today's attendance for a classroom
     * 
     * @param classroomId Classroom ID
     * @return API response with today's attendance
     */
    @GetMapping("/today/{classroomId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getTodayAttendance(
            @PathVariable Long classroomId) {
        
        try {
            List<Attendance> attendanceList = attendanceService.getTodayAttendance(classroomId);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Today's attendance retrieved successfully", attendanceList));
        } catch (Exception e) {
            log.error("Error retrieving today's attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve today's attendance"));
        }
    }

    /**
     * Get attendance statistics
     * 
     * @param date Date for statistics
     * @return API response with statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AttendanceService.AttendanceStats>> getAttendanceStats(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        try {
            AttendanceService.AttendanceStats stats = attendanceService.getAttendanceStats(date);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Attendance statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving attendance statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance statistics"));
        }
    }
}
