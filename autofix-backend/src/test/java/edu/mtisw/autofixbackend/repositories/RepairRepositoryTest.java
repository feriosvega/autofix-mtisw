package edu.mtisw.autofixbackend.repositories;

import edu.mtisw.autofixbackend.entities.RepairDetailEntity;
import edu.mtisw.autofixbackend.entities.RepairEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepairRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepairRepository repairRepository;

    private final java.util.Map<Long, Long> vehicleIds = new java.util.HashMap<>();

    // Crea (una vez) un vehículo real para respetar la FK repairs.vehicle_id -> vehicles.id
    private Long vehicle(Long key) {
        return vehicleIds.computeIfAbsent(key, k -> entityManager.persistAndFlush(new edu.mtisw.autofixbackend.entities.VehicleEntity(
                null, "ABCD" + (10 + k), "Toyota", "Yaris", edu.mtisw.autofixbackend.entities.VehicleType.SEDAN,
                2020, edu.mtisw.autofixbackend.entities.EngineType.GASOLINA, 5)).getId());
    }

    private RepairEntity persist(Long vehicleKey, LocalDate entry, long bonus) {
        RepairEntity r = new RepairEntity();
        r.setVehicleId(vehicle(vehicleKey));
        r.setEntryDate(entry);
        r.setEntryTime(LocalTime.of(10, 0));
        r.setDetails(new java.util.ArrayList<>(List.of(new RepairDetailEntity(null, 1, 120_000))));
        r.setApplyBonus(bonus > 0);
        r.setBonusDiscount(bonus);
        return entityManager.persistAndFlush(r);
    }

    @Test
    void whenCountRepairsInLast12Months_thenExcludesOldAndCurrent() {
        persist(1L, LocalDate.of(2025, 9, 1), 0);   // fuera de los 12 meses
        persist(1L, LocalDate.of(2026, 3, 1), 0);
        persist(1L, LocalDate.of(2026, 9, 1), 0);
        persist(2L, LocalDate.of(2026, 9, 1), 0);   // otro vehículo
        RepairEntity current = persist(1L, LocalDate.of(2026, 10, 5), 0);

        long count = repairRepository.countRepairsInPeriod(vehicle(1L), LocalDate.of(2025, 10, 5), LocalDate.of(2026, 10, 5), current.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void whenCountBonusesUsedInMonth_thenOnlyThatMonth() {
        persist(1L, LocalDate.of(2026, 10, 2), 70_000);
        persist(1L, LocalDate.of(2026, 9, 2), 70_000);

        long used = repairRepository.countBonusesUsed(vehicle(1L), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), -1L);

        assertThat(used).isEqualTo(1);
    }

    @Test
    void whenFindByEntryDateBetween_thenOrdered() {
        persist(1L, LocalDate.of(2026, 10, 20), 0);
        persist(2L, LocalDate.of(2026, 10, 3), 0);
        persist(3L, LocalDate.of(2026, 11, 3), 0);
        entityManager.clear();   // fuerza a leer desde la BD (incluida la relación @ManyToOne)

        List<RepairEntity> found = repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertThat(found).hasSize(2).extracting(RepairEntity::getVehicleId).containsExactly(vehicle(2L), vehicle(1L));
        assertThat(found.get(0).getVehicle().getPlate()).isEqualTo("ABCD12");  // relación N:1 cargada
        assertThat(found.get(0).getDetails()).hasSize(1);
    }
}
