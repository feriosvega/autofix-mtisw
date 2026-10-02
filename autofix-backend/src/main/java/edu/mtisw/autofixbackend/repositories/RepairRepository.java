package edu.mtisw.autofixbackend.repositories;

import edu.mtisw.autofixbackend.entities.RepairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepairRepository extends JpaRepository<RepairEntity, Long> {

    List<RepairEntity> findByVehicleIdOrderByEntryDateDesc(Long vehicleId);

    // Ingresos (reparaciones) dentro de un rango de fechas: base de los reportes R1..R4
    List<RepairEntity> findByEntryDateBetweenOrderByEntryDateAsc(LocalDate from, LocalDate to);

    // Nro. de reparaciones del vehículo en los últimos 12 meses (sin contar la reparación actual)
    @Query("SELECT COUNT(r) FROM RepairEntity r WHERE r.vehicleId = :vehicleId " +
            "AND r.entryDate >= :from AND r.entryDate <= :to AND r.id <> :excludeId")
    long countRepairsInPeriod(@Param("vehicleId") Long vehicleId,
                              @Param("from") LocalDate from,
                              @Param("to") LocalDate to,
                              @Param("excludeId") Long excludeId);

    // ¿El vehículo ya usó un bono en este mes? (solo un bono por vehículo)
    @Query("SELECT COUNT(r) FROM RepairEntity r WHERE r.vehicleId = :vehicleId AND r.applyBonus = true " +
            "AND r.bonusDiscount > 0 AND r.entryDate >= :from AND r.entryDate <= :to " +
            "AND r.id <> :excludeId")
    long countBonusesUsed(@Param("vehicleId") Long vehicleId,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to,
                          @Param("excludeId") Long excludeId);

    boolean existsByVehicleId(Long vehicleId);
}
