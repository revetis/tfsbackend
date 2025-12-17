package com.example.tfs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.tfs.exceptions.RateLimitExceededException;
import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(value = "com.example.apps")
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<?> handleRateLimitExceededException(RateLimitExceededException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(ApiTemplate.apiTemplateGenerator(false, 429, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 429,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, List<String>> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(fieldError -> errors.put(fieldError.getField(),
                                List.of(fieldError.getDefaultMessage())));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiTemplate.apiTemplateGenerator(false, 400, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 400,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiTemplate.apiTemplateGenerator(false, 403, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 403,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<?> handleJWTException(JwtException ex, HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiTemplate.apiTemplateGenerator(false, 401, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 401,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiTemplate.apiTemplateGenerator(false, 400, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 400,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<?> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                log.error("RuntimeException: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiTemplate.apiTemplateGenerator(false, 500, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 500,
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                log.error("Exception: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiTemplate.apiTemplateGenerator(false, 500, request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, 500,
                                                                request.getRequestURI(), errors),
                                                null));
        }
}
