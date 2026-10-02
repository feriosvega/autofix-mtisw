package edu.mtisw.autofixbackend.repositories;

import edu.mtisw.autofixbackend.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByPlate(String plate);

    boolean existsByPlate(String plate);
}
