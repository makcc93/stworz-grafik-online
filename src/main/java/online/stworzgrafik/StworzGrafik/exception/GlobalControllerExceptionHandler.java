package online.stworzgrafik.StworzGrafik.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> entityNotFound(EntityNotFoundException e){
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> requestBodyException(HttpMessageNotReadableException e){
        return ResponseEntity.badRequest().body("Request body is missing or json is incorrect");
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<String> entityAlreadyExist(EntityExistsException e){
        return ResponseEntity.badRequest().body("Entity with this name already exist");
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> nameContainsIllegalChars(ValidationException e){
        return ResponseEntity.badRequest().body("Name contains illegal char(s)");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> others(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
    }
}
