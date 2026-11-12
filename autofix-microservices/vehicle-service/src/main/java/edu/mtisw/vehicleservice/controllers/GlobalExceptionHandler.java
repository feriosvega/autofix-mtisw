package edu.mtisw.vehicleservice.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
    // Error al llamar a otro microservicio (HTTP 4xx/5xx o servicio no disponible)
    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    public ResponseEntity<Map<String, String>> handleRemote(org.springframework.web.client.RestClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "Error al comunicarse con otro microservicio: " + ex.getMessage()));
    }
}
