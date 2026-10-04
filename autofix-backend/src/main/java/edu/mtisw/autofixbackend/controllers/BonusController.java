package edu.mtisw.autofixbackend.controllers;

import edu.mtisw.autofixbackend.entities.BonusEntity;
import edu.mtisw.autofixbackend.services.BonusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Bonos TopCar: todos pueden consultarlos (para aplicarlos en HU2); solo ADMIN los administra
@RestController
@RequestMapping("/api/v1/bonuses")
public class BonusController {
    @Autowired
    BonusService bonusService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<BonusEntity>> listBonuses() {
        return ResponseEntity.ok(bonusService.getBonuses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<BonusEntity> saveBonus(@RequestBody BonusEntity bonus) {
        return ResponseEntity.ok(bonusService.saveBonus(bonus));
    }

    // Body: {"text": "- Toyota: 5 bonos de 70.000 pesos ...", "year": 2026, "month": 10}
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/email")
    public ResponseEntity<List<BonusEntity>> registerFromEmail(@RequestBody Map<String, Object> body) {
        String text = String.valueOf(body.get("text"));
        int year = Integer.parseInt(String.valueOf(body.get("year")));
        int month = Integer.parseInt(String.valueOf(body.get("month")));
        return ResponseEntity.ok(bonusService.registerFromEmail(text, year, month));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBonus(@PathVariable Long id) {
        bonusService.deleteBonus(id);
        return ResponseEntity.noContent().build();
    }
}
