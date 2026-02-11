package pharmacie.config;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
// Define the ApiError class or use an existing one
class ApiError {
    private HttpStatus status;
    private String message;
    private String details;
}
