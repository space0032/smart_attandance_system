package com.attendance.service;

import com.attendance.util.FaceDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for face recognition operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceRecognitionService {

    private final FaceDetector faceDetector;
    
    private static final double RECOGNITION_THRESHOLD = 0.6;

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
            log.warn("Invalid face image provided for encoding extraction");
            return new byte[0];
        }

        try {
            // Convert face image to byte array (simplified encoding)
            // In production, use proper face recognition models like OpenCV's LBPHFaceRecognizer
            // or deep learning models for better accuracy
            byte[] encoding = faceDetector.matToByteArray(faceImage);
            
            log.debug("Extracted face encoding of size: {} bytes", encoding.length);
            return encoding;
        } catch (Exception e) {
            log.error("Error extracting face encoding", e);
            return new byte[0];
        }
    }

    /**
     * Recognize a face by comparing encodings
     * 
     * @param faceEncoding Face encoding to recognize
     * @param storedEncodings List of stored face encodings from database
     * @return Index of matched encoding, -1 if no match found
     */
    public int recognizeFace(byte[] faceEncoding, List<byte[]> storedEncodings) {
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
            
            if (distance < minDistance && distance < RECOGNITION_THRESHOLD) {
                minDistance = distance;
                matchIndex = i;
            }
        }

        if (matchIndex >= 0) {
            log.info("Face recognized with confidence: {}", 1.0 - minDistance);
        } else {
            log.info("No matching face found");
        }

        return matchIndex;
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

        // Normalize to same length
        int minLength = Math.min(encoding1.length, encoding2.length);
        if (minLength == 0) {
            return Double.MAX_VALUE;
        }

        double sumSquaredDiff = 0.0;
        
        for (int i = 0; i < minLength; i++) {
            double diff = (encoding1[i] & 0xFF) - (encoding2[i] & 0xFF);
            sumSquaredDiff += diff * diff;
        }

        // Normalize by length and scale to 0-1 range
        double distance = Math.sqrt(sumSquaredDiff / minLength) / 255.0;
        
        return Math.min(distance, 1.0);
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
