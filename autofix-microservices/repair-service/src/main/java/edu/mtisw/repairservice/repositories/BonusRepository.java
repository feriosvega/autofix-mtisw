package edu.mtisw.repairservice.repositories;

import edu.mtisw.repairservice.entities.BonusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonusRepository extends JpaRepository<BonusEntity, Long> {
    Optional<BonusEntity> findByBrandIgnoreCaseAndBonusYearAndBonusMonth(String brand, int year, int month);

    List<BonusEntity> findAllByOrderByBonusYearDescBonusMonthDescBrandAsc();
}
