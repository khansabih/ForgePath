package io.github.khansabih.forgepath.common.error;

import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceNotFoundException;
import io.github.khansabih.forgepath.catalog.team.exception.TeamAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(TeamAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleTeamAlreadyExists(TeamAlreadyExistsException e,
                                                            HttpServletRequest request){
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                e.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e,
                                                     HttpServletRequest request){
        Map<String, String> validationErrors = new LinkedHashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach((fieldError) -> {
                   validationErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
                });

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request.getRequestURI(),
                validationErrors
        );
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<ApiError> handleTeamNotFound(TeamNotFoundException e,
                                                       HttpServletRequest request){
        return buildErrorResponse(
          HttpStatus.NOT_FOUND,
          e.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatus status, String message, String path,
            Map<String, String> validationErrors
    ){
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );

        return ResponseEntity.status(status).body(apiError);
    }

//    FOR CATALOG SERVICE CONTROLLER EXCEPTION.

    @ExceptionHandler(PlatformServiceAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleServiceAlreadyExists(
            PlatformServiceAlreadyExistsException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(PlatformServiceNotFoundException.class)
    public ResponseEntity<ApiError> handleServiceNotFound(
            PlatformServiceNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

}
