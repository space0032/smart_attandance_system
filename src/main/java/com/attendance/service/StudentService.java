package com.attendance.service;

import com.attendance.dto.StudentDTO;
import com.attendance.model.Student;
import com.attendance.repository.StudentRepository;
import com.attendance.util.FaceDetector;
import com.attendance.util.InputValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service for student management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final FaceDetector faceDetector;
    private final InputValidationService inputValidationService;

    private static final String UPLOAD_DIR = "uploads/faces/";

    /**
     * Register a new student with face image
     * 
     * @param studentDTO Student data
     * @param faceImage  Face image file
     * @return Registered student
     * @throws IOException              if file operations fail
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Student registerStudentWithFace(StudentDTO studentDTO, MultipartFile faceImage)
            throws IOException {

        // Validate and sanitize student data using the validation-sanitizer library
        inputValidationService.validateAndSanitizeStudent(studentDTO);

        // Check for duplicate student ID
        if (studentRepository.existsByStudentId(studentDTO.getStudentId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }

        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Process face image
        byte[] faceEncoding = null;
        String faceImagePath = null;

        if (faceImage != null && !faceImage.isEmpty()) {
            // Detect face in image
            Mat image = faceDetector.byteArrayToMat(faceImage.getBytes());
            List<Rect> faces = faceRecognitionService.detectFaces(image);

            if (faces.isEmpty()) {
                throw new IllegalArgumentException("No face detected in the image");
            }

            if (faces.size() > 1) {
                log.warn("Multiple faces detected, using the first one");
            }

            // Extract face and generate encoding
            Mat extractedFace = faceDetector.extractFace(image, faces.get(0));
            faceEncoding = faceRecognitionService.extractFaceEncoding(extractedFace);

            // Save face image
            faceImagePath = saveFaceImage(extractedFace, studentDTO.getStudentId());
        }

        // Create student entity
        Student student = new Student();
        student.setStudentId(studentDTO.getStudentId());
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setEmail(studentDTO.getEmail());
        student.setDepartment(studentDTO.getDepartment());
        student.setFaceEncoding(faceEncoding);
        student.setFaceImagePath(faceImagePath);

        Student savedStudent = studentRepository.save(student);
        log.info("Registered student: {}", savedStudent.getStudentId());

        return savedStudent;
    }

    /**
     * Save face image to file system
     * 
     * @param faceImage Face image
     * @param studentId Student ID
     * @return File path
     * @throws IOException if save fails
     */
    private String saveFaceImage(Mat faceImage, String studentId) throws IOException {
        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = studentId + "_" + UUID.randomUUID().toString() + ".jpg";
        Path filePath = uploadPath.resolve(filename);

        // Save image
        byte[] imageData = faceDetector.matToByteArray(faceImage);
        Files.write(filePath, imageData);

        return filePath.toString();
    }

    /**
     * Get all students
     * 
     * @return List of all students
     */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Get student by ID
     * 
     * @param id Student ID
     * @return Student entity
     * @throws IllegalArgumentException if student not found
     */
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
    }

    /**
     * Update student information
     * 
     * @param id         Student ID
     * @param studentDTO Updated student data
     * @return Updated student
     * @throws IllegalArgumentException if student not found or validation fails
     */
    @Transactional
    public Student updateStudent(Long id, StudentDTO studentDTO) {
        // Validate and sanitize student data using the validation-sanitizer library
        inputValidationService.validateAndSanitizeStudent(studentDTO);

        Student student = getStudentById(id);

        // Check if studentId is being changed and if it's unique
        if (!student.getStudentId().equals(studentDTO.getStudentId()) &&
                studentRepository.existsByStudentId(studentDTO.getStudentId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }

        // Check if email is being changed and if it's unique
        if (!student.getEmail().equals(studentDTO.getEmail()) &&
                studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        student.setStudentId(studentDTO.getStudentId());
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setEmail(studentDTO.getEmail());
        student.setDepartment(studentDTO.getDepartment());

        Student updatedStudent = studentRepository.save(student);
        log.info("Updated student: {}", updatedStudent.getStudentId());

        return updatedStudent;
    }

    /**
     * Delete student
     * 
     * @param id Student ID
     */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);

        // Delete face image file if exists
        if (student.getFaceImagePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(student.getFaceImagePath()));
            } catch (IOException e) {
                log.error("Failed to delete face image file", e);
            }
        }

        studentRepository.delete(student);
        log.info("Deleted student: {}", student.getStudentId());
    }

    /**
     * Get student by student ID
     * 
     * @param studentId Student ID
     * @return Student entity
     */
    @Transactional(readOnly = true)
    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with studentId: " + studentId));
    }
}
