package edu.mtisw.repairlistservice.controllers;

import edu.mtisw.repairlistservice.entities.EngineType;
import edu.mtisw.repairlistservice.entities.RepairTypeEntity;
import edu.mtisw.repairlistservice.services.RepairTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// M2 - HU2: Registrar lista de reparaciones con sus respectivos precios
@RestController
@RequestMapping("/api/v1/repair-types")
public class RepairTypeController {
    @Autowired
    RepairTypeService repairTypeService;

    @GetMapping("/")
    public ResponseEntity<List<RepairTypeEntity>> listRepairTypes() {
        return ResponseEntity.ok(repairTypeService.getRepairTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairTypeEntity> getRepairTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(repairTypeService.getRepairTypeById(id));
    }

    // Precio de una reparación para un tipo de motor (lo usa M3 al registrar una reparación)
    @GetMapping("/{id}/price/{engineType}")
    public ResponseEntity<Long> getPrice(@PathVariable Long id, @PathVariable EngineType engineType) {
        return ResponseEntity.ok(repairTypeService.getPrice(id, engineType));
    }

    @PostMapping("/")
    public ResponseEntity<RepairTypeEntity> saveRepairType(@RequestBody RepairTypeEntity repairType) {
        return ResponseEntity.ok(repairTypeService.saveRepairType(repairType));
    }

    @PutMapping("/")
    public ResponseEntity<RepairTypeEntity> updateRepairType(@RequestBody RepairTypeEntity repairType) {
        return ResponseEntity.ok(repairTypeService.updateRepairType(repairType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairTypeById(@PathVariable Long id) {
        repairTypeService.deleteRepairType(id);
        return ResponseEntity.noContent().build();
    }
}
