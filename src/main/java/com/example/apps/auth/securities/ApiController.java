package com.example.apps.auth.securities;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.interfaces.RateLimit;

@RestController
public class ApiController {

    @RateLimit(capacity = 10, refillTokens = 10, refillDuration = 1)
    @GetMapping("/openapi.yaml")
    public String getYaml() {
        return "YAML içeriği";
    }

    @RateLimit(capacity = 2, refillTokens = 2, refillDuration = 1)
    @GetMapping("/redoc.html")
    public String getRedoc() {
        return "<html>Redoc page</html>";
    }
}
