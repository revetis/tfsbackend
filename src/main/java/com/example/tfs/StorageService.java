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
