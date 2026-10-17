package com.TrialCompass.TrialCompass.exception;

import jakarta.websocket.OnClose;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice //catches exceptions thrown by any controller, decides what client sees
public class GlobalExceptionHandler {

    //this will run when a @valid check fails
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);

        //returns bad request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //runs when service throws email already exists exception
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(EmailAlreadyExistsException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());

        //returns conflict - when theirs conflicting data
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    //runs when login fails
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(InvalidCredentialsException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());

        //401 unauthorized message
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    //unusable search resulting in a 400 bad request code
    @ExceptionHandler(InvalidSearchException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSearch(InvalidSearchException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //outside service is down display a 502 bad gateway
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalFailure(ExternalServiceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
