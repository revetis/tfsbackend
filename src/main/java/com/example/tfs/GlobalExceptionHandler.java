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

import com.example.apps.products.exceptions.MainCategoryException;
import com.example.apps.products.exceptions.SubCategoryException;
import com.example.tfs.exceptions.*;
import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(value = "com.example.apps")
@Slf4j
public class GlobalExceptionHandler {

        private ResponseEntity<?> buildResponse(Exception ex, HttpServletRequest request, HttpStatus status) {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", ex.getMessage());

                if (status.is5xxServerError())
                        log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

                return ResponseEntity.status(status)
                                .body(ApiTemplate.apiTemplateGenerator(
                                                false,
                                                status.value(),
                                                request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false, status.value(),
                                                                request.getRequestURI(), errors),
                                                null));
        }

        @ExceptionHandler({
                        MainCategoryException.class,
                        SubCategoryException.class,

        })
        public ResponseEntity<?> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<?> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.TOO_MANY_REQUESTS);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, List<String>> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(err -> errors.put(err.getField(), List.of(err.getDefaultMessage())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiTemplate.apiTemplateGenerator(
                                                false,
                                                HttpStatus.BAD_REQUEST.value(),
                                                request.getRequestURI(),
                                                ApiErrorTemplate.apiErrorTemplateGenerator(false,
                                                                HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                                                                errors),
                                                null));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<?> handleJWTException(JwtException ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex,
                        HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<?> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
                return buildResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
