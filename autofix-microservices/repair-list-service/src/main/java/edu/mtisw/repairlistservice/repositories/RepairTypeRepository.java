package edu.mtisw.repairlistservice.repositories;

import edu.mtisw.repairlistservice.entities.RepairTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairTypeRepository extends JpaRepository<RepairTypeEntity, Long> {

    List<RepairTypeEntity> findAllByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);
}
