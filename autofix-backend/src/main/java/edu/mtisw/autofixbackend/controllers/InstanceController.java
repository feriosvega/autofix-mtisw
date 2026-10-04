package edu.mtisw.autofixbackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Map;

// Endpoint público para evidenciar el balanceo de carga: cada réplica responde con su hostname (id del contenedor)
@RestController
@RequestMapping("/api/v1/public")
public class InstanceController {

    @GetMapping("/instance")
    public ResponseEntity<Map<String, String>> instance() throws Exception {
        return ResponseEntity.ok(Map.of("instance", InetAddress.getLocalHost().getHostName()));
    }
}
