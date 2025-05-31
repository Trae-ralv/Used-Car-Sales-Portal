package com.autoxpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Service to handle file storage operations, such as saving images.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:classpath:uploads/}")
    private String uploadDir;

    // Valid image extensions
    private static final List<String> VALID_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    /**
     * Saves an image file to the configured upload directory.
     * @param file The MultipartFile to save.
     * @param fileName The desired file name (e.g., "1_1.jpg").
     * @return The relative path to the saved file (e.g., "/images/uploads/1_1.jpg").
     * @throws IOException If file operations fail.
     * @throws IllegalArgumentException If the file format is invalid.
     */
    public String saveImage(MultipartFile file, String fileName) throws IOException {
        logger.debug("Saving image: {}", fileName);

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!VALID_EXTENSIONS.contains(fileExtension)) {
            logger.warn("Invalid image format: {}", fileExtension);
            throw new IllegalArgumentException("Invalid image format: " + fileExtension + ". Allowed: " + VALID_EXTENSIONS);
        }

        // Resolve upload directory
        Path uploadPath = resolveUploadPath();
        Path filePath = uploadPath.resolve(fileName);

        // Create directories if they don't exist
        Files.createDirectories(filePath.getParent());
        logger.debug("Upload path created: {}", filePath.getParent());

        // Save the file
        Files.write(filePath, file.getBytes());
        logger.info("Image saved successfully: {}", filePath);

        return "/images/uploads/" + fileName;
    }

    /**
     * Resolves the upload directory path based on configuration.
     * @return The resolved Path.
     * @throws IOException If the path cannot be resolved.
     */
    private Path resolveUploadPath() throws IOException {
        if (uploadDir.startsWith("classpath:")) {
            // Development: Use src/main/resources/uploads/
            return Paths.get(resourceLoader.getResource("classpath:uploads/").getFile().getAbsolutePath());
        } else {
            // Production: Use external directory
            return Paths.get(uploadDir);
        }
    }

    /**
     * Extracts the file extension from the original filename.
     * @param originalFilename The original filename.
     * @return The lowercase extension (e.g., "jpg") or "jpg" if none found.
     */
    private String getFileExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "jpg"; // Default to jpg
    }
}