package pharmacie.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

/**
 * The GlobalExceptionHandler class handles exceptions thrown by the application and provides custom response bodies
 * with detailed error messages in JSON format.
 * This class is annotated with @RestControllerAdvice, which allows it to handle exceptions globally for all REST controllers
 * and automatically serialize responses to JSON.
 * It contains exception handler methods for various types of exceptions:
 * - handleConstraintViolationException: Handles ConstraintViolationException and constructs a custom response body
 *   with a validation error message.
 * - handleDataIntegrityViolationException: Handles DataIntegrityViolationException and constructs a custom response body
 *   with a data integrity violation message.
 * All methods return a ResponseEntity object containing the custom response body and the appropriate HTTP status code.
 */
@RestControllerAdvice(basePackages = "pharmacie")
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        // Collect the error messages of all violations
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        // Construct a custom response body or use a predefined one
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error: " + errorMessage, errorMessage);

        // Return a ResponseEntity containing the custom response body and HTTP status
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles a DataIntegrityViolationException and constructs a custom response
     * body with a detailed message.
     *
     * @param ex The DataIntegrityViolationException that was thrown
     * @return A ResponseEntity containing the custom response body and HTTP status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String specificMessage = ex.getMostSpecificCause().getMessage();

        // Construct a custom response body with the detailed message
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                "The operation could not be completed due to a data integrity violation: " + specificMessage);

        // Return a ResponseEntity containing the custom response body and HTTP status
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex) {
        // Construct a custom response body with a not found message
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                ex.getMessage());


        // Return a ResponseEntity containing the custom response body and HTTP status
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
    // Construct a custom response body with an illegal state message
    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST,
            "Illegal state error",
            ex.getMessage());

    // Return a ResponseEntity containing the custom response body and HTTP status
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Construct a custom response body with an illegal argument message
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Illegal argument error",
                ex.getMessage());

        // Return a ResponseEntity containing the custom response body and HTTP status
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        // Construct a custom response body with a general error message
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                ex.getMessage());

        // Return a ResponseEntity containing the custom response body and HTTP status
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
