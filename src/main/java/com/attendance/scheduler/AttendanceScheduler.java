package com.attendance.scheduler;

import com.attendance.model.CameraConfig;
import com.attendance.repository.CameraConfigRepository;
import com.attendance.service.CameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final CameraConfigRepository cameraConfigRepository;
    private final CameraService cameraService;

    private final Map<Long, Instant> lastCaptureShortcuts = new ConcurrentHashMap<>();

    /**
     * Run every 1 second to check if any camera needs processing
     */
    @Scheduled(fixedRate = 1000)
    public void processCameras() {
        List<CameraConfig> configs = cameraConfigRepository.findAll();
        Instant now = Instant.now();

        for (CameraConfig config : configs) {
            if (!config.isActive()) {
                continue;
            }

            Instant lastRun = lastCaptureShortcuts.getOrDefault(config.getId(), Instant.MIN);
            long secondsSinceLastRun = java.time.Duration.between(lastRun, now).getSeconds();

            if (secondsSinceLastRun >= config.getCalculatedIntervalSeconds()) {
                try {
                    cameraService.captureAndProcess(config);
                    lastCaptureShortcuts.put(config.getId(), now);
                } catch (Exception e) {
                    log.error("Error in scheduler for camera: {}", config.getClassroom().getCourseCode(), e);
                }
            }
        }
    }
}
