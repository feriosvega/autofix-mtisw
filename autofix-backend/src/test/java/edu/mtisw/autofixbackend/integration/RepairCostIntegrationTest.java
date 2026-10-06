package edu.mtisw.autofixbackend.integration;

import edu.mtisw.autofixbackend.entities.*;
import edu.mtisw.autofixbackend.repositories.BonusRepository;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import edu.mtisw.autofixbackend.repositories.VehicleRepository;
import edu.mtisw.autofixbackend.services.RepairService;
import edu.mtisw.autofixbackend.services.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Prueba de integración: servicios + repositorios + BD H2 reales (sin mocks)
@SpringBootTest
@ActiveProfiles("test")
class RepairCostIntegrationTest {

    @Autowired
    private RepairService repairService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private RepairRepository repairRepository;
    @Autowired
    private BonusRepository bonusRepository;

    @BeforeEach
    void setUp() {
        repairRepository.deleteAll();
        vehicleRepository.deleteAll();
        bonusRepository.deleteAll();
    }

    @Test
    void whenRegisterRepairWithBonus_thenCostIsCalculatedStoredAndBonusConsumed() {
        VehicleEntity v = vehicleRepository.save(
                new VehicleEntity(null, "ABCD12", "Toyota", "Corolla", VehicleType.SEDAN, 2015, EngineType.GASOLINA, 5));
        bonusRepository.save(new BonusEntity(null, "Toyota", 2026, 10, 5, 70_000, null));

        RepairEntity r = new RepairEntity();
        r.setVehicleId(v.getId());
        r.setEntryDate(LocalDate.of(2026, 10, 5));
        r.setEntryTime(LocalTime.of(10, 0));
        r.setExitDate(LocalDate.of(2026, 10, 7));
        r.setExitTime(LocalTime.of(15, 0));
        r.setPickupDate(LocalDate.of(2026, 10, 9));
        r.setPickupTime(LocalTime.of(11, 0));
        r.setMileage(15_000);
        r.setApplyBonus(true);
        r.setDetails(new ArrayList<>(List.of(new RepairDetailEntity(null, 1, 0), new RepairDetailEntity(null, 5, 0))));

        RepairEntity saved = repairService.saveRepair(r);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTotalAmount()).isEqualTo(289_408);
        assertThat(bonusRepository.findAll().get(0).getQuantity()).isEqualTo(4);
        assertThat(reportService.getReportR1(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))
                .getTotals().getTotalAmount()).isEqualTo(289_408);
    }
}
