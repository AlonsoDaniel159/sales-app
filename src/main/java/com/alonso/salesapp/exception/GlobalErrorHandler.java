package com.alonso.salesapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalErrorHandler {

    // Manejar cuando no encontramos algo (404 Not Found)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ModelNotFoundException.class)
    @ResponseBody
    public ErrorResponse handleModelNotFound(ModelNotFoundException ex, WebRequest req) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                req.getDescription(false),
                null
        );
    }

    // Manejar validaciones (@Valid) fallidas (400 Bad Request)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorResponse handleBadRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        String message = "Validation failed with " + errors.size() + " error(s).";

        return new ErrorResponse(
                message,
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                errors
        );
    }

    // Manejar cualquier otro error no previsto (500 Internal Server Error)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleAllExceptions(Exception ex, HttpServletRequest request) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
    }


    @ExceptionHandler(CloudinaryException.class)
    public ResponseEntity<ErrorResponse> handleCloudinaryException(CloudinaryException e, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                LocalDateTime.now(),
                e.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Manejar credenciales inválidas (401 Unauthorized)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public ErrorResponse handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
    }

    // Manejar usuario no encontrado (404 Not Found)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ErrorResponse handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
    }

    // Manejar token inválido (401 Unauthorized)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseBody
    public ErrorResponse handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
    }

    // Manejar usuario ya existente (409 Conflict)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseBody
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        return new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                null
        );
    }

}
