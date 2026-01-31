package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.StudentDTO;
import com.attendance.model.Student;
import com.attendance.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for student management
 */
@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

        private final StudentService studentService;

        /**
         * Register a new student with face image
         * 
         * @param studentDTO Student data
         * @param faceImage  Face image file
         * @return API response with registered student
         */
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<Student>> registerStudent(
                        @Valid @ModelAttribute StudentDTO studentDTO,
                        @RequestParam(value = "faceImage", required = false) MultipartFile faceImage) {

                try {
                        Student student = studentService.registerStudentWithFace(studentDTO, faceImage);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Student registered successfully", student));
                } catch (IllegalArgumentException e) {
                        log.error("Validation error during student registration", e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error(e.getMessage()));
                } catch (Exception e) {
                        log.error("Error registering student", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error("Failed to register student: " + e.getMessage()));
                }
        }

        /**
         * Get all students
         * 
         * @return API response with list of students
         */
        @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
                try {
                        List<Student> students = studentService.getAllStudents();
                        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
                } catch (Exception e) {
                        log.error("Error retrieving students", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error("Failed to retrieve students"));
                }
        }

        /**
         * Get student by ID
         * 
         * @param id Student ID
         * @return API response with student
         */
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable Long id) {
                try {
                        Student student = studentService.getStudentById(id);
                        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", student));
                } catch (IllegalArgumentException e) {
                        log.error("Student not found", e);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(e.getMessage()));
                } catch (Exception e) {
                        log.error("Error retrieving student", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error("Failed to retrieve student"));
                }
        }

        /**
         * Update student information
         * 
         * @param id         Student ID
         * @param studentDTO Updated student data
         * @return API response with updated student
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<Student>> updateStudent(
                        @PathVariable Long id,
                        @Valid @RequestBody StudentDTO studentDTO) {

                try {
                        Student student = studentService.updateStudent(id, studentDTO);
                        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", student));
                } catch (IllegalArgumentException e) {
                        log.error("Validation error during student update", e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error(e.getMessage()));
                } catch (Exception e) {
                        log.error("Error updating student", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error("Failed to update student"));
                }
        }

        /**
         * Delete student
         * 
         * @param id Student ID
         * @return API response
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
                try {
                        studentService.deleteStudent(id);
                        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
                } catch (IllegalArgumentException e) {
                        log.error("Student not found", e);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ApiResponse.error(e.getMessage()));
                } catch (Exception e) {
                        log.error("Error deleting student", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponse.error("Failed to delete student"));
                }
        }
}
