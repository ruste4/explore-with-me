package ru.practicum.explorewithme;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse notFoundExceptionHandle(NotFoundException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .reason(e.getReason())
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder stringForLogger = new StringBuilder();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            stringForLogger.append(fieldName + ": " + errorMessage + "; ");
        });

        log.warn(stringForLogger.toString());

        return errors;
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse constraintViolationExceptionHandle(ConstraintViolationException e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .message(e.getMessage())
                .reason(e.getSQLException().getMessage())
                .status(HttpStatus.CONFLICT)
                .build();
    }

    @Data
    @Builder
    private static class ErrorResponse {

        private String message;

        private String reason;

        private HttpStatus status;

        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();
    }
}
