package ar.edu.utn.dds.k3003.config;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> body(String error, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", error);
        payload.put("message", message);
        payload.put("timestamp", OffsetDateTime.now());
        return payload;
    }

    // ---------- 400 Bad Request ----------
    @ExceptionHandler({
            IllegalArgumentException.class,
            NumberFormatException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            BindException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + (error.getDefaultMessage() == null ? "valor invalido" : error.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
            if (message.isBlank()) {
                message = "Solicitud invalida";
            }
        } else if (e instanceof ConstraintViolationException constraintViolationException) {
            message = constraintViolationException.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            if (message.isBlank()) {
                message = "Solicitud invalida";
            }
        } else if (e instanceof MethodArgumentTypeMismatchException mismatchException) {
            String requiredType = mismatchException.getRequiredType() != null
                    ? mismatchException.getRequiredType().getSimpleName()
                    : "tipo esperado";
            message = "El parametro '" + mismatchException.getName() + "' debe ser de tipo " + requiredType;
        } else if (e instanceof NumberFormatException) {
            message = "Formato invalido para un identificador numerico";
        } else if (e instanceof HttpMessageNotReadableException notReadable && notReadable.getCause() != null) {
            message = notReadable.getCause().getMessage();
        } else {
            message = e.getMessage();
        }

        if (message == null || message.isBlank()) {
            message = "Solicitud invalida";
        }

        log.warn("400 Bad Request: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Bad Request", message));
    }

    // ---------- 404 Not Found ----------
    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception e) {
        String message = e.getMessage() == null || e.getMessage().isBlank() ? "Recurso no encontrado" : e.getMessage();
        log.info("404 Not Found: {}", message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Not Found", message));
    }

    // ---------- 405 Method Not Allowed ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("405 Method Not Allowed: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Method Not Allowed", e.getMessage()));
    }

    // ---------- 409 Conflict ----------
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException e) {
        String message = e.getMessage() == null || e.getMessage().isBlank() ? "No se pudo procesar la solicitud" : e.getMessage();
        log.warn("409 Conflict: {}", message);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Conflict", message));
    }

    // ---------- 415 Unsupported Media Type ----------
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        log.warn("415 Unsupported Media Type: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Unsupported Media Type", e.getMessage()));
    }

    // ---------- ResponseStatusException ----------
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e) {
        HttpStatusCode statusCode = e.getStatusCode();
        String message = e.getReason() == null || e.getReason().isBlank() ? "Error en la solicitud" : e.getReason();
        String label = statusCode instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : statusCode.toString();
        log.warn("{} {}: {}", statusCode.value(), label, message);
        return ResponseEntity
                .status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(label, message));
    }

    // ---------- 500 Internal Server Error (fallback) ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        log.error("500 Internal Server Error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("Internal Server Error", "Ocurrio un error inesperado"));
    }
}
