package com.example.tfs;

import com.example.tfs.maindto.ApiErrorTemplate;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.example")
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
                log.error("Exception intercepted: {} - Message: {}", ex.getClass().getSimpleName(), ex.getMessage());

                HttpStatus status = resolveHttpStatus(ex);
                String message = resolveMessage(ex);
                String path = request.getDescription(false).replace("uri=", "");

                return ResponseEntity
                                .status(status)
                                .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                false,
                                                status.value(),
                                                path,
                                                message));
        }

        private HttpStatus resolveHttpStatus(Exception ex) {
                ResponseStatus annotation = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

                if (annotation != null) {
                        return annotation.code();
                }

                return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        private String resolveMessage(Exception ex) {
                if (ex.getMessage() == null || ex.getMessage().isEmpty()) {
                        return "An unexpected error occurred in the payment system";
                }
                return ex.getMessage();
        }
}