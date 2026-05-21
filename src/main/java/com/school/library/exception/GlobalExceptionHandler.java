package com.school.library.exception;

import com.school.library.dto.WebResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WebResponse<Object>> handleResourceNotFound(ResourceNotFoundException exception) {
        WebResponse<Object> response = WebResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .status("NOT_FOUND")
                .errors(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<WebResponse<Object>> handleBadRequest(BadRequestException exception) {
        WebResponse<Object> response = WebResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status("BAD_REQUEST")
                .errors(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WebResponse<Object>> handleValidationException(MethodArgumentNotValidException exception) {
        StringBuilder builder = new StringBuilder();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            builder.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        
        WebResponse<Object> response = WebResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status("BAD_REQUEST")
                .errors(builder.toString())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResponse<Object>> handleGeneralException(Exception exception) {
        WebResponse<Object> response = WebResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status("INTERNAL_SERVER_ERROR")
                .errors(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
