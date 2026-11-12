package tn.esprit.wafik_lahbib_4cce10.web.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.wafik_lahbib_4cce10.exception.ResourceNotFoundException;

// Traduit les exceptions des services en réponses HTTP avec un corps JSON lisible
// (statut, message, horodatage). Les contrôleurs ne fabriquent jamais eux-mêmes les 404 / 400.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return corps(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return corps(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ex. supprimer une agence qui a encore des véhicules (clé étrangère) : 409 au lieu d'un 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        return corps(HttpStatus.CONFLICT,
                "Opération impossible : la ressource est liée à d'autres données");
    }

    private ResponseEntity<Map<String, Object>> corps(HttpStatus statut, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut", statut.value());
        body.put("message", message);
        body.put("horodatage", LocalDateTime.now().toString());
        return ResponseEntity.status(statut).body(body);
    }
}
