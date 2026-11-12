package edu.mtisw.repairservice.controllers;

import edu.mtisw.repairservice.entities.BonusEntity;
import edu.mtisw.repairservice.services.BonusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// M3 - Bonos TopCar (descuento por bonos del cálculo de HU3)
@RestController
@RequestMapping("/api/v1/bonuses")
public class BonusController {
    @Autowired
    BonusService bonusService;

    @GetMapping("/")
    public ResponseEntity<List<BonusEntity>> listBonuses() {
        return ResponseEntity.ok(bonusService.getBonuses());
    }

    @PostMapping("/")
    public ResponseEntity<BonusEntity> saveBonus(@RequestBody BonusEntity bonus) {
        return ResponseEntity.ok(bonusService.saveBonus(bonus));
    }

    // Body: {"text": "- Toyota: 5 bonos de 70.000 pesos ...", "year": 2026, "month": 10}
    @PostMapping("/email")
    public ResponseEntity<List<BonusEntity>> registerFromEmail(@RequestBody Map<String, Object> body) {
        String text = String.valueOf(body.get("text"));
        int year = Integer.parseInt(String.valueOf(body.get("year")));
        int month = Integer.parseInt(String.valueOf(body.get("month")));
        return ResponseEntity.ok(bonusService.registerFromEmail(text, year, month));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBonus(@PathVariable Long id) {
        bonusService.deleteBonus(id);
        return ResponseEntity.noContent().build();
    }
}
