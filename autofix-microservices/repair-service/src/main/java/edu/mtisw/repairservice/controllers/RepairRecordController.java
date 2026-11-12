package edu.mtisw.repairservice.controllers;

import edu.mtisw.repairservice.dtos.RepairReportDataDto;
import edu.mtisw.repairservice.dtos.VehicleHistoryDto;
import edu.mtisw.repairservice.entities.RepairRecordEntity;
import edu.mtisw.repairservice.services.RepairRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/repairs")
public class RepairRecordController {
    @Autowired private RepairRecordService service;

    @GetMapping({"", "/"})
    public ResponseEntity<List<RepairRecordEntity>> list() { return ResponseEntity.ok(service.findAll()); }

    @GetMapping("/history")
    public ResponseEntity<List<VehicleHistoryDto>> history() { return ResponseEntity.ok(service.history()); }

    @GetMapping("/report-data")
    public ResponseEntity<List<RepairReportDataDto>> reportData() { return ResponseEntity.ok(service.reportData()); }

    @GetMapping("/vehicle/{vehicleId}/exists")
    public ResponseEntity<Boolean> vehicleHasRepairs(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.vehicleHasRepairs(vehicleId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<RepairRecordEntity>> byVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.findByVehicle(vehicleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairRecordEntity> get(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }

    @PostMapping({"", "/"})
    public ResponseEntity<RepairRecordEntity> create(@RequestBody RepairRecordEntity repair) {
        return ResponseEntity.ok(service.create(repair));
    }

    @PutMapping({"", "/"})
    public ResponseEntity<RepairRecordEntity> update(@RequestBody RepairRecordEntity repair) {
        return ResponseEntity.ok(service.update(repair));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<RepairRecordEntity> calculate(@PathVariable Long id) {
        return ResponseEntity.ok(service.calculate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
