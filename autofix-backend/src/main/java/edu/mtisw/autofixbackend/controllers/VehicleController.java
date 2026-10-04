package edu.mtisw.autofixbackend.controllers;

import edu.mtisw.autofixbackend.entities.VehicleEntity;
import edu.mtisw.autofixbackend.services.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU1 - Registro de vehículos (USER y ADMIN)
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    @Autowired
    VehicleService vehicleService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<VehicleEntity>> listVehicles() {
        return ResponseEntity.ok(vehicleService.getVehicles());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleEntity> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleEntity> getVehicleByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(vehicleService.getVehicleByPlate(plate));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/")
    public ResponseEntity<VehicleEntity> saveVehicle(@RequestBody VehicleEntity vehicle) {
        return ResponseEntity.ok(vehicleService.saveVehicle(vehicle));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/")
    public ResponseEntity<VehicleEntity> updateVehicle(@RequestBody VehicleEntity vehicle) {
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicle));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleById(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
