package com.attendance.util;

import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for face detection using OpenCV
 */
@Slf4j
@Component
public class FaceDetector {

    private CascadeClassifier faceCascade;
    private static final String HAAR_CASCADE_FILE = "haarcascade_frontalface_default.xml";

    public FaceDetector() {
        try {
            // Load OpenCV native library
            OpenCV.loadLocally();
            
            // Load Haar Cascade classifier
            loadHaarCascade();
            
            log.info("FaceDetector initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize FaceDetector", e);
        }
    }

    private void loadHaarCascade() {
        try {
            // Try to load from resources
            InputStream is = getClass().getClassLoader().getResourceAsStream(HAAR_CASCADE_FILE);
            
            if (is != null) {
                // Create temp file
                File tempFile = File.createTempFile("haarcascade", ".xml");
                tempFile.deleteOnExit();
                
                Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                faceCascade = new CascadeClassifier(tempFile.getAbsolutePath());
                is.close();
            } else {
                log.warn("Haar cascade file not found in resources, using default path");
                faceCascade = new CascadeClassifier();
            }
            
            if (faceCascade.empty()) {
                log.error("Failed to load Haar Cascade classifier");
            }
        } catch (IOException e) {
            log.error("Error loading Haar Cascade", e);
        }
    }

    /**
     * Detect faces in an image
     * 
     * @param image OpenCV Mat image
     * @return List of detected face rectangles
     */
    public List<Rect> detectFaces(Mat image) {
        List<Rect> faces = new ArrayList<>();
        
        if (image.empty() || faceCascade == null || faceCascade.empty()) {
            log.warn("Invalid image or cascade classifier not loaded");
            return faces;
        }

        try {
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayImage, grayImage);

            MatOfRect faceDetections = new MatOfRect();
            faceCascade.detectMultiScale(grayImage, faceDetections, 1.1, 4, 0,
                    new Size(30, 30), new Size());

            faces = faceDetections.toList();
            
            log.info("Detected {} face(s)", faces.size());
        } catch (Exception e) {
            log.error("Error detecting faces", e);
        }

        return faces;
    }

    /**
     * Extract face region from image
     * 
     * @param image OpenCV Mat image
     * @param faceRect Rectangle defining face region
     * @return Cropped face image
     */
    public Mat extractFace(Mat image, Rect faceRect) {
        if (image.empty() || faceRect == null) {
            return new Mat();
        }

        try {
            // Ensure the rectangle is within image bounds
            int x = Math.max(0, faceRect.x);
            int y = Math.max(0, faceRect.y);
            int width = Math.min(faceRect.width, image.cols() - x);
            int height = Math.min(faceRect.height, image.rows() - y);

            Rect boundedRect = new Rect(x, y, width, height);
            Mat faceImage = new Mat(image, boundedRect);
            
            // Resize to standard size for consistency
            Mat resizedFace = new Mat();
            Imgproc.resize(faceImage, resizedFace, new Size(128, 128));
            
            return resizedFace;
        } catch (Exception e) {
            log.error("Error extracting face", e);
            return new Mat();
        }
    }

    /**
     * Convert byte array to OpenCV Mat
     * 
     * @param imageData Image byte array
     * @return OpenCV Mat
     */
    public Mat byteArrayToMat(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return new Mat();
        }

        try {
            MatOfByte matOfByte = new MatOfByte(imageData);
            return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
        } catch (Exception e) {
            log.error("Error converting byte array to Mat", e);
            return new Mat();
        }
    }

    /**
     * Convert OpenCV Mat to byte array
     * 
     * @param image OpenCV Mat
     * @return Image byte array
     */
    public byte[] matToByteArray(Mat image) {
        if (image.empty()) {
            return new byte[0];
        }

        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, matOfByte);
            return matOfByte.toArray();
        } catch (Exception e) {
            log.error("Error converting Mat to byte array", e);
            return new byte[0];
        }
    }
}
