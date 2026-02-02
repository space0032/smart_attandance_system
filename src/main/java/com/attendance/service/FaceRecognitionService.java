package com.attendance.service;

import com.attendance.util.FaceDetector;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;

/**
 * Service for face recognition operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceRecognitionService {

    private final FaceDetector faceDetector;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final double RECOGNITION_THRESHOLD = 0.5; // Stricter threshold for Python encodings
    private static final String PYTHON_SERVICE_URL = "http://localhost:5000";

    @Data
    private static class EncodingResponse {
        private List<Float> encoding;
        private boolean found;
    }

    /**
     * Detect faces in an image
     * 
     * @param image OpenCV Mat image
     * @return List of detected face rectangles
     */
    public List<Rect> detectFaces(Mat image) {
        if (image == null || image.empty()) {
            log.warn("Invalid image provided for face detection");
            return List.of();
        }

        return faceDetector.detectFaces(image);
    }

    /**
     * Extract face encoding/features from a face image
     * 
     * @param faceImage OpenCV Mat containing face region
     * @return Byte array representing face encoding
     */
    public byte[] extractFaceEncoding(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            return new byte[0];
        }

        try {
            // Convert to JPG bytes
            byte[] imageBytes = faceDetector.matToByteArray(faceImage);

            // Prepare Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "face.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Python Service
            EncodingResponse response = restTemplate.postForObject(
                    PYTHON_SERVICE_URL + "/encode", requestEntity, EncodingResponse.class);

            if (response != null && response.isFound() && response.getEncoding() != null) {
                return floatListToByteArray(response.getEncoding());
            }

        } catch (Exception e) {
            log.warn("Failed to call Python AI service: {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * Recognize a face by comparing encodings
     * 
     * @param faceEncoding    Face encoding to recognize
     * @param storedEncodings List of stored face encodings from database
     * @param threshold       Similarity threshold (lower is stricter)
     * @return Index of matched encoding, -1 if no match found
     */
    public int recognizeFace(byte[] faceEncoding, List<byte[]> storedEncodings, double threshold) {
        if (faceEncoding == null || faceEncoding.length == 0 || storedEncodings == null) {
            return -1;
        }

        double minDistance = Double.MAX_VALUE;
        int matchIndex = -1;

        for (int i = 0; i < storedEncodings.size(); i++) {
            byte[] stored = storedEncodings.get(i);
            if (stored == null || stored.length == 0) {
                continue;
            }

            double distance = calculateEuclideanDistance(faceEncoding, stored);

            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                matchIndex = i;
            }
        }

        if (matchIndex >= 0) {
            log.info("Face recognized with confidence: {}", 1.0 - minDistance);
        } else {
            log.info("No matching face found (best distance: {}, threshold: {})", minDistance, threshold);
        }

        return matchIndex;
    }

    /**
     * Recognize a face using default threshold
     */
    public int recognizeFace(byte[] faceEncoding, List<byte[]> storedEncodings) {
        return recognizeFace(faceEncoding, storedEncodings, RECOGNITION_THRESHOLD);
    }

    /**
     * Calculate Euclidean distance between two encodings
     * 
     * @param encoding1 First encoding
     * @param encoding2 Second encoding
     * @return Normalized Euclidean distance (0-1)
     */
    public double calculateEuclideanDistance(byte[] encoding1, byte[] encoding2) {
        if (encoding1 == null || encoding2 == null) {
            return Double.MAX_VALUE;
        }

        // Check if legacy (JPG bytes) vs New (512 bytes)
        // If lengths differ significantly, it's a mismatch
        if (encoding1.length != encoding2.length) {
            return 1.5; // Arbitrary high distance
        }

        try {
            float[] vec1 = byteArrayToFloatArray(encoding1);
            float[] vec2 = byteArrayToFloatArray(encoding2);

            double sum = 0.0;
            for (int i = 0; i < vec1.length; i++) {
                double diff = vec1[i] - vec2[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private byte[] floatListToByteArray(List<Float> floats) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (float f : floats) {
            dos.writeFloat(f);
        }
        return bos.toByteArray();
    }

    private float[] byteArrayToFloatArray(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = dis.readFloat();
        }
        return floats;
    }

    /**
     * Calculate confidence score for face match
     * 
     * @param encoding1 First encoding
     * @param encoding2 Second encoding
     * @return Confidence score (0-1, higher is better)
     */
    public double calculateConfidence(byte[] encoding1, byte[] encoding2) {
        double distance = calculateEuclideanDistance(encoding1, encoding2);
        return 1.0 - distance;
    }

    /**
     * Check if face encoding is valid
     * 
     * @param encoding Face encoding
     * @return true if valid, false otherwise
     */
    public boolean isValidEncoding(byte[] encoding) {
        return encoding != null && encoding.length > 0;
    }
}
