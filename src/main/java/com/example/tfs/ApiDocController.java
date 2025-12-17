package com.example.tfs;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiDocController {
    @GetMapping(path = "/openapi.yaml", produces = "application/x-yaml")
    public ResponseEntity<Resource> getYaml() {
        Resource yaml = new ClassPathResource("static/openapi.yaml");
        if (!yaml.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(yaml);
    }

}
