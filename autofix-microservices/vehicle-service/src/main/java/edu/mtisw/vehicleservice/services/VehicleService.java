package edu.mtisw.vehicleservice.services;

import edu.mtisw.vehicleservice.entities.VehicleEntity;
import edu.mtisw.vehicleservice.repositories.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;

// M1 - HU1: Registro de vehículos
@Service
public class VehicleService {

    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z]{4}[0-9]{2}$");

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    RestTemplate restTemplate;

    // El historial de reparaciones vive en M3 (repair-service): se consulta por HTTP
    static final String REPAIR_SERVICE_URL = "http://repair-service/api/v1/repairs";

    public List<VehicleEntity> getVehicles() {
        return vehicleRepository.findAll();
    }

    public VehicleEntity getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + id));
    }

    public VehicleEntity getVehicleByPlate(String plate) {
        return vehicleRepository.findByPlate(normalizePlate(plate))
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + plate));
    }

    public VehicleEntity saveVehicle(VehicleEntity vehicle) {
        vehicle.setId(null);
        validate(vehicle);
        if (vehicleRepository.existsByPlate(vehicle.getPlate())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la patente " + vehicle.getPlate());
        }
        return vehicleRepository.save(vehicle);
    }

    public VehicleEntity updateVehicle(VehicleEntity vehicle) {
        VehicleEntity current = getVehicleById(vehicle.getId());
        validate(vehicle);
        if (!current.getPlate().equals(vehicle.getPlate()) && vehicleRepository.existsByPlate(vehicle.getPlate())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la patente " + vehicle.getPlate());
        }
        return vehicleRepository.save(vehicle);
    }

    public boolean deleteVehicle(Long id) {
        getVehicleById(id);
        if (hasRepairs(id)) {
            throw new IllegalStateException("No se puede eliminar: el vehículo tiene reparaciones en su historial");
        }
        vehicleRepository.deleteById(id);
        return true;
    }

    public boolean hasRepairs(Long vehicleId) {
        Boolean exists = restTemplate.getForObject(REPAIR_SERVICE_URL + "/vehicle/" + vehicleId + "/exists", Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    // Patente chilena antigua: 4 letras y 2 números. Se aceptan "abcd-12", "ABCD 12", "ABCD12".
    public String normalizePlate(String plate) {
        if (plate == null) {
            return null;
        }
        return plate.toUpperCase().replaceAll("[\\s.\\-·]", "");
    }

    private void validate(VehicleEntity vehicle) {
        vehicle.setPlate(normalizePlate(vehicle.getPlate()));
        if (vehicle.getPlate() == null || !PLATE_PATTERN.matcher(vehicle.getPlate()).matches()) {
            throw new IllegalArgumentException("Patente inválida: debe tener 4 letras y 2 números (ej. ABCD12)");
        }
        if (isBlank(vehicle.getBrand()) || isBlank(vehicle.getModel())) {
            throw new IllegalArgumentException("Marca y modelo son obligatorios");
        }
        if (vehicle.getVehicleType() == null || vehicle.getEngineType() == null) {
            throw new IllegalArgumentException("Tipo de vehículo y tipo de motor son obligatorios");
        }
        int maxYear = Year.now().getValue() + 1;
        if (vehicle.getManufactureYear() < 1900 || vehicle.getManufactureYear() > maxYear) {
            throw new IllegalArgumentException("Año de fabricación inválido");
        }
        if (vehicle.getSeats() < 1 || vehicle.getSeats() > 60) {
            throw new IllegalArgumentException("Número de asientos inválido");
        }
        vehicle.setBrand(vehicle.getBrand().trim());
        vehicle.setModel(vehicle.getModel().trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
