package com.attendance.controller;

import com.attendance.model.CameraConfig;
import com.attendance.model.Classroom;
import com.attendance.repository.CameraConfigRepository;
import com.attendance.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final ClassroomRepository classroomRepository;
    private final CameraConfigRepository cameraConfigRepository;

    @GetMapping("/settings")
    public String showSettings(Model model) {
        List<Classroom> classrooms = classroomRepository.findAll();
        List<CameraConfig> configs = cameraConfigRepository.findAll();

        // Map configs by classroom ID for easy lookup
        Map<Long, CameraConfig> configMap = configs.stream()
                .collect(Collectors.toMap(c -> c.getClassroom().getId(), Function.identity()));

        model.addAttribute("classrooms", classrooms);
        model.addAttribute("configMap", configMap);

        return "settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam Long classroomId,
            @RequestParam String rtspUrl,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "60") int lectureDurationMinutes,
            @RequestParam(defaultValue = "4") int snapshotsPerLecture,
            @RequestParam(defaultValue = "false") boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            Classroom classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid classroom ID"));

            CameraConfig config = cameraConfigRepository.findByClassroomId(classroomId)
                    .orElse(new CameraConfig());

            config.setClassroom(classroom);
            config.setRtspUrl(rtspUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setLectureDurationMinutes(lectureDurationMinutes);
            config.setSnapshotsPerLecture(snapshotsPerLecture);
            config.setActive(active);

            cameraConfigRepository.save(config);
            redirectAttributes.addFlashAttribute("successMessage", "Settings saved for " + classroom.getCourseCode());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving settings: " + e.getMessage());
        }

        return "redirect:/settings";
    }
}
