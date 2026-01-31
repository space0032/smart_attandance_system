package com.attendance.service;

import com.attendance.model.Attendance;
import com.attendance.model.Attendance.AttendanceStatus;
import com.attendance.model.Classroom;
import com.attendance.model.Student;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.ClassroomRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.util.FaceDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for attendance management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final FaceDetector faceDetector;

    /**
     * Process camera feed image and mark attendance for recognized students
     * 
     * @param imageFile   Camera image file
     * @param classroomId Classroom ID
     * @return List of attendance records created
     * @throws IOException if image processing fails
     */
    @Transactional
    public List<Attendance> processCameraFeed(MultipartFile imageFile, Long classroomId)
            throws IOException {

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found with id: " + classroomId));

        // Convert image to Mat
        byte[] imageBytes = imageFile.getBytes();
        Mat image = faceDetector.byteArrayToMat(imageBytes);

        if (image.empty()) {
            throw new IllegalArgumentException("Invalid image data");
        }

        return processFaceProcessing(image, classroom);
    }

    /**
     * Process OpenCV Mat image for face recognition and attendance
     * 
     * @param image     OpenCV Mat image
     * @param classroom Classroom entity
     * @return List of attendance records
     */
    @Transactional
    public List<Attendance> processFaceProcessing(Mat image, Classroom classroom) {
        // Detect faces in image
        List<Rect> faces = faceRecognitionService.detectFaces(image);
        log.info("Detected {} face(s) in camera feed", faces.size());

        List<Attendance> attendanceRecords = new ArrayList<>();

        // Get all students with face encodings
        List<Student> allStudents = studentRepository.findAll();
        List<Student> studentsWithFaces = allStudents.stream()
                .filter(s -> s.getFaceEncoding() != null && s.getFaceEncoding().length > 0)
                .toList();

        // Process each detected face
        for (Rect faceRect : faces) {
            Mat faceImage = faceDetector.extractFace(image, faceRect);
            byte[] faceEncoding = faceRecognitionService.extractFaceEncoding(faceImage);

            // Try to recognize the face
            List<byte[]> storedEncodings = studentsWithFaces.stream()
                    .map(Student::getFaceEncoding)
                    .toList();

            int matchIndex = faceRecognitionService.recognizeFace(faceEncoding, storedEncodings);

            if (matchIndex >= 0) {
                Student recognizedStudent = studentsWithFaces.get(matchIndex);

                // Check if attendance already marked
                if (!isAttendanceMarkedToday(recognizedStudent.getId(), classroom.getId())) {
                    double confidence = faceRecognitionService.calculateConfidence(
                            faceEncoding, recognizedStudent.getFaceEncoding());

                    Attendance attendance = markAttendance(recognizedStudent, classroom, confidence);
                    attendanceRecords.add(attendance);

                    log.info("Marked attendance for student: {} with confidence: {}",
                            recognizedStudent.getStudentId(), confidence);
                }
            }
        }

        return attendanceRecords;
    }

    /**
     * Mark attendance for a student
     * 
     * @param student         Student entity
     * @param classroom       Classroom entity
     * @param confidenceScore Face recognition confidence score
     * @return Created attendance record
     */
    @Transactional
    public Attendance markAttendance(Student student, Classroom classroom, double confidenceScore) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Determine attendance status based on time
        AttendanceStatus status = determineAttendanceStatus(now, classroom);

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setClassroom(classroom);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendance.setConfidenceScore(confidenceScore);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance marked for student {} in classroom {}",
                student.getStudentId(), classroom.getCourseCode());

        return savedAttendance;
    }

    /**
     * Determine attendance status based on check-in time
     * 
     * @param checkInTime Check-in time
     * @param classroom   Classroom entity
     * @return Attendance status
     */
    private AttendanceStatus determineAttendanceStatus(LocalTime checkInTime, Classroom classroom) {
        LocalTime startTime = classroom.getStartTime();

        if (startTime == null) {
            return AttendanceStatus.PRESENT;
        }

        // Late if more than 15 minutes after start time
        LocalTime lateThreshold = startTime.plusMinutes(15);

        if (checkInTime.isAfter(lateThreshold)) {
            return AttendanceStatus.LATE;
        }

        return AttendanceStatus.PRESENT;
    }

    /**
     * Check if attendance is already marked for today
     * 
     * @param studentId   Student ID
     * @param classroomId Classroom ID
     * @return true if already marked, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isAttendanceMarkedToday(Long studentId, Long classroomId) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository
                .findByStudentIdAndClassroomIdAndAttendanceDate(studentId, classroomId, today);

        return existing.isPresent();
    }

    /**
     * Get attendance records by date and classroom
     * 
     * @param date        Attendance date
     * @param classroomId Classroom ID
     * @return List of attendance records
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDateAndClassroom(LocalDate date, Long classroomId) {
        return attendanceRepository.findByAttendanceDateAndClassroomId(date, classroomId);
    }

    /**
     * Get today's attendance for a classroom
     * 
     * @param classroomId Classroom ID
     * @return List of attendance records
     */
    @Transactional(readOnly = true)
    public List<Attendance> getTodayAttendance(Long classroomId) {
        return getAttendanceByDateAndClassroom(LocalDate.now(), classroomId);
    }

    /**
     * Get attendance statistics for a date
     * 
     * @param date Date
     * @return Map of statistics
     */
    @Transactional(readOnly = true)
    public AttendanceStats getAttendanceStats(LocalDate date) {
        long totalAttendance = attendanceRepository.countByDate(date);
        long presentCount = attendanceRepository.countPresentByDate(date);

        return new AttendanceStats(totalAttendance, presentCount);
    }

    /**
     * Inner class for attendance statistics
     */
    public record AttendanceStats(long total, long present) {
        public double getAttendanceRate() {
            return total > 0 ? (double) present / total * 100 : 0.0;
        }
    }
}
