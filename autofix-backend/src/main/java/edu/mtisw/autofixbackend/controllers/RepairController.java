package edu.mtisw.autofixbackend.controllers;

import edu.mtisw.autofixbackend.entities.RepairEntity;
import edu.mtisw.autofixbackend.services.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU2 - Registro de reparaciones  /  HU3 - Cálculo del costo total (USER y ADMIN)
@RestController
@RequestMapping("/api/v1/repairs")
public class RepairController {
    @Autowired
    RepairService repairService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<RepairEntity>> listRepairs() {
        return ResponseEntity.ok(repairService.getRepairs());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RepairEntity> getRepairById(@PathVariable Long id) {
        return ResponseEntity.ok(repairService.getRepairById(id));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<RepairEntity>> listRepairsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(repairService.getRepairsByVehicle(vehicleId));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/")
    public ResponseEntity<RepairEntity> saveRepair(@RequestBody RepairEntity repair) {
        return ResponseEntity.ok(repairService.saveRepair(repair));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/")
    public ResponseEntity<RepairEntity> updateRepair(@RequestBody RepairEntity repair) {
        return ResponseEntity.ok(repairService.updateRepair(repair));
    }

    // HU3: calcula (o recalcula) el costo total de la reparación
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/calculate")
    public ResponseEntity<RepairEntity> calculateRepair(@PathVariable Long id) {
        return ResponseEntity.ok(repairService.calculateRepair(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairById(@PathVariable Long id) {
        repairService.deleteRepair(id);
        return ResponseEntity.noContent().build();
    }
}
