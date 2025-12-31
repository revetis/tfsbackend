package com.example.tfs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StorageService {

    @Autowired
    private ApplicationProperties applicationProperties;

    public Boolean uploadFile(MultipartFile file, String uploadDir, String fileName) {
        try {
            Path uploadsPath = Paths.get(applicationProperties.getSTATIC_PATH() + "/" + "uploads");
            Path uploadPath = Paths.get(applicationProperties.getSTATIC_PATH() + "/" + uploadDir);

            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
            }
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            return true;
        } catch (Exception e) {
            log.error("Error uploading file: " + e.getMessage());
            return false;
        }
    }

    public String store(MultipartFile file, String folder) {
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = System.currentTimeMillis() + "_" + Math.random() + extension;

            // Create upload directory
            Path uploadPath = Paths.get(applicationProperties.getSTATIC_PATH() + "/uploads/" + folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Files.copy(file.getInputStream(), uploadPath.resolve(filename));
            log.info("File uploaded successfully: " + filename);

            return filename;
        } catch (Exception e) {
            log.error("Error storing file: " + e.getMessage());
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    public Boolean deleteFile(String filePath) {
        try {
            Path fileToDelete = Paths.get(applicationProperties.getSTATIC_PATH(), filePath.replaceFirst("^/", ""));

            return Files.deleteIfExists(fileToDelete);
        } catch (Exception e) {
            log.error("Error deleting file: " + e.getMessage());
            return false;
        }
    }

}
