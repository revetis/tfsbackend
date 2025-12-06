package com.example.exception;

import com.example.configuration.GlobalApiResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public List<String> addMapValue(List<String> list, String newValue){
        list.add(newValue);
        return list;
    }

    private <T> ApiError<T> ApiErrorGenerator(T errors){
        ApiError<T> resultError = new ApiError<>();
        resultError.setId(UUID.randomUUID());
        resultError.setDate(new Date());
        resultError.setErrors(errors);
        return resultError;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        Map<String, List<String>> errors = new HashMap<>();
        for (ObjectError error:e.getBindingResult().getAllErrors()){
            String fieldName = ((FieldError)error).getField();
            if (errors.containsKey(fieldName)){
                errors.put(fieldName,addMapValue(errors.get(fieldName), error.getDefaultMessage()));
            } else {
                errors.put(fieldName, addMapValue(new ArrayList<>(), error.getDefaultMessage()));
            }
        }
        return ResponseEntity.badRequest().body(GlobalApiResult.generate(ApiErrorGenerator(errors),null));
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage());
        return ResponseEntity.badRequest().body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public ResponseEntity<?> emptyResultDataAccessExceptionHandler(EmptyResultDataAccessException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.status(404).body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<?> accessDeniedExceptionHandler(AccessDeniedException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.status(403).body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> methodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.status(405).body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> mediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.status(415).body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<?> messageNotReadableExceptionHandler(HttpMessageNotReadableException e){
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.badRequest().body(GlobalApiResult.generate(apiError,null));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception e) {
        ApiError<String> apiError = ApiErrorGenerator(e.getMessage());
        return ResponseEntity.internalServerError().body(GlobalApiResult.generate(apiError,null));
    }

}
