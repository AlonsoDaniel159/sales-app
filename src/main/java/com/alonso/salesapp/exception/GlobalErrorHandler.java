package com.alonso.salesapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorHandler {

    // 1. Manejar cuando no encontramos algo (404 Not Found)
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModelNotFound(ModelNotFoundException ex, WebRequest req) {
        ErrorResponse res = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                req.getDescription(false) // false para no mostrar info sensible del cliente
        );
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    // 2. Manejar validaciones (@Valid) fallidas (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(MethodArgumentNotValidException ex, WebRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" "));

        ErrorResponse res = new ErrorResponse(
                LocalDateTime.now(),
                message,
                req.getDescription(false)
        );
        return ResponseEntity.badRequest().body(res);
    }

    // 3. Manejar cualquier otro error no previsto (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest req) {
        ErrorResponse res = new ErrorResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                req.getDescription(false)
        );
        return ResponseEntity.internalServerError().body(res);
    }

}
