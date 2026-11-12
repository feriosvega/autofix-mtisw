package edu.mtisw.vehicleservice.repositories;

import edu.mtisw.vehicleservice.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    Optional<VehicleEntity> findByPlate(String plate);

    boolean existsByPlate(String plate);
}
