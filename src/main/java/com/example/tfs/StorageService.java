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
            // Validate Image Magic Bytes
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            if (isImageExtension(extension)) {
                if (!isValidImage(file)) {
                    throw new RuntimeException(
                            "Invalid image file format (Magic Bytes check failed). Only authentic JPG/PNG files are allowed.");
                }
            }

            // Generate unique filename
            String filename = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString() + extension;

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

    private boolean isImageExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png");
    }

    private boolean isValidImage(MultipartFile file) {
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4)
                return false;

            // Check PEG (FF D8 FF)
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return true;
            }
            // Check PNG (89 50 4E 47 0D 0A 1A 0A)
            if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E
                    && header[3] == (byte) 0x47) {
                return true;
            }
            return false;
        } catch (java.io.IOException e) {
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
