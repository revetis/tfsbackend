package com.example.tfs;

import com.example.tfs.maindto.ApiErrorTemplate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.example")
@Slf4j
public class GlobalExceptionHandler {
        @ExceptionHandler(java.util.IllegalFormatConversionException.class)
        public ResponseEntity<?> handleFormatConversionException(
                        java.util.IllegalFormatConversionException ex,
                        WebRequest request) {

                log.error("Format exception intercepted! Expected value : {}, Actual value: {}",
                                ex.getConversion(), ex.getClass().getSimpleName());

                String path = request.getDescription(false).replace("uri=", "");

                // Efendim, burada hatanın hangi karakterden (%d, %s vb.) kaynaklandığını da
                // kullanıcıya raporluyoruz
                String detailedMessage = String.format(
                                "Type exception: '%c' conversion does not match argument type. For more information, please check the logs.",
                                ex.getConversion());

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                false,
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                path,
                                                detailedMessage));
        }

        @ExceptionHandler(com.example.apps.shipments.exceptions.ShipmentException.class)
        public ResponseEntity<?> handleShipmentException(
                        com.example.apps.shipments.exceptions.ShipmentException ex,
                        WebRequest request) {

                log.error("Shipment Operation Failed: {}", ex.getMessage());
                String path = request.getDescription(false).replace("uri=", "");

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                                                false,
                                                HttpStatus.BAD_REQUEST.value(),
                                                path,
                                                ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiErrorTemplate<?> handleValidation(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                return ApiErrorTemplate.apiErrorTemplateGenerator(false, HttpStatus.BAD_REQUEST.value(),
                                request.getDescription(false).replace("uri=", ""), errors);
        }

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