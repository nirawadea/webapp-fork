package com.neu.edu.cloudapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        return new ResponseEntity<>("HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED);
    }
}
