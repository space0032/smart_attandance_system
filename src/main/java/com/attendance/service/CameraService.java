package com.attendance.service;

import com.attendance.model.Attendance;
import com.attendance.model.CameraConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CameraService {

    private final AttendanceService attendanceService;

    /**
     * Capture frame from active camera and process attendance
     * 
     * @param config Camera configuration
     */
    public void captureAndProcess(CameraConfig config) {
        if (!config.isActive()) {
            return;
        }

        String fullUrl = constructUrl(config);
        VideoCapture capture = new VideoCapture();

        try {
            log.debug("Attempting to connect to camera: {}", config.getClassroom().getCourseCode());

            // Check if URL is a single digit (for local webcam index)
            if (fullUrl.matches("\\d+")) {
                capture.open(Integer.parseInt(fullUrl));
            } else {
                capture.open(fullUrl);
            }

            if (!capture.isOpened()) {
                log.error("Failed to open camera stream for classroom: {}", config.getClassroom().getCourseCode());
                return;
            }

            Mat frame = new Mat();
            if (capture.read(frame)) {
                if (!frame.empty()) {
                    List<Attendance> saved = attendanceService.processFaceProcessing(frame, config.getClassroom());
                    if (!saved.isEmpty()) {
                        log.info("Marked {} record(s) from camera stream for {}",
                                saved.size(), config.getClassroom().getCourseCode());
                    }
                } else {
                    log.warn("Captured empty frame from camera: {}", config.getClassroom().getCourseCode());
                }
            } else {
                log.warn("Failed to read frame from camera: {}", config.getClassroom().getCourseCode());
            }

        } catch (Exception e) {
            log.error("Error processing camera stream for {}", config.getClassroom().getCourseCode(), e);
        } finally {
            if (capture.isOpened()) {
                capture.release();
            }
        }
    }

    private String constructUrl(CameraConfig config) {
        String url = config.getRtspUrl();
        if (config.getUsername() != null && !config.getUsername().isEmpty() &&
                config.getPassword() != null && !config.getPassword().isEmpty() &&
                url.startsWith("rtsp://") && !url.contains("@")) {

            // Inject credentials: rtsp://user:pass@ip...
            return url.replace("rtsp://", "rtsp://" + config.getUsername() + ":" + config.getPassword() + "@");
        }
        return url;
    }
}
