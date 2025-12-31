package com.example.apps.auths.controllers;

import com.example.tfs.StorageService;
import com.example.tfs.maindto.ApiTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rest/api/public")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder) {

        try {
            String filename = storageService.store(file, folder);
            // Return full URL with backend base
            String fileUrl = "http://localhost:8080/uploads/" + folder + "/" + filename;

            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                    true,
                    HttpStatus.SC_OK,
                    "/upload",
                    null,
                    fileUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiTemplate.apiTemplateGenerator(
                    false,
                    HttpStatus.SC_BAD_REQUEST,
                    "/upload",
                    e.getMessage(),
                    null));
        }
    }
}
