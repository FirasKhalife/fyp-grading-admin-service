package com.fypgrading.adminservice.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.ws.rs.ForbiddenException;

@ControllerAdvice
public class RestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler
    protected ResponseEntity<ExceptionResponse> handleAuthException(AuthException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    protected ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler
    protected ResponseEntity<ExceptionResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        logger.warn(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler
    protected ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
