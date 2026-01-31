package com.attendance.util;

import com.attendance.model.Attendance;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating Excel files from attendance data
 */
@Slf4j
@Component
public class ExcelGenerator {

    private static final String[] HEADERS = {
        "Student ID", "Full Name", "Email", "Department", "Check-in Time", "Status"
    };

    /**
     * Generate Excel file from attendance records
     * 
     * @param attendanceList List of attendance records
     * @param date Date for the attendance records
     * @return ByteArrayInputStream containing Excel file
     * @throws IOException if file generation fails
     */
    public ByteArrayInputStream generateAttendanceExcel(List<Attendance> attendanceList, LocalDate date) 
            throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Attendance");

            // Create header row
            createHeaderRow(workbook, sheet);

            // Fill data rows
            int rowIdx = 1;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowIdx++);
                
                row.createCell(0).setCellValue(attendance.getStudent().getStudentId());
                row.createCell(1).setCellValue(attendance.getStudent().getFullName());
                row.createCell(2).setCellValue(attendance.getStudent().getEmail());
                row.createCell(3).setCellValue(attendance.getStudent().getDepartment());
                row.createCell(4).setCellValue(attendance.getCheckInTime().format(timeFormatter));
                row.createCell(5).setCellValue(attendance.getStatus().toString());
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Generated Excel file with {} attendance records", attendanceList.size());
            
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Create and style the header row
     * 
     * @param workbook Excel workbook
     * @param sheet Excel sheet
     */
    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create header cells
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Generate filename for attendance Excel export
     * 
     * @param date Date for the attendance
     * @return Formatted filename
     */
    public String generateFilename(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("attendance_%s.xlsx", date.format(formatter));
    }
}
