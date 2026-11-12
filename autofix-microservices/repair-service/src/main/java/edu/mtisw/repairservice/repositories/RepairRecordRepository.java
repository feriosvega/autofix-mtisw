package edu.mtisw.repairservice.repositories;

import edu.mtisw.repairservice.entities.RepairRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface RepairRecordRepository extends JpaRepository<RepairRecordEntity, Long> {
    List<RepairRecordEntity> findByVehicleIdOrderByEntryDateDescEntryTimeDesc(Long vehicleId);
    long countByVehicleIdAndEntryDateBetweenAndIdNot(Long vehicleId, LocalDate from, LocalDate to, Long ignoredId);
    boolean existsByVehicleId(Long vehicleId);

    // ¿El vehículo ya usó un bono en el mes? (solo se puede aplicar un bono por vehículo)
    @Query("SELECT COUNT(r) FROM RepairRecordEntity r WHERE r.vehicleId = :vehicleId AND r.bonusDiscount > 0 " +
            "AND r.entryDate >= :from AND r.entryDate <= :to AND r.id <> :excludeId")
    long countBonusesUsed(@Param("vehicleId") Long vehicleId, @Param("from") LocalDate from,
                          @Param("to") LocalDate to, @Param("excludeId") Long excludeId);
}
