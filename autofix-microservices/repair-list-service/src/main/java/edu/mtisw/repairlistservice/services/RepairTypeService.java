package edu.mtisw.repairlistservice.services;

import edu.mtisw.repairlistservice.entities.EngineType;
import edu.mtisw.repairlistservice.entities.RepairTypeEntity;
import edu.mtisw.repairlistservice.repositories.RepairTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// M2 - HU2: lista de reparaciones con sus respectivos precios
@Service
public class RepairTypeService {

    @Autowired
    RepairTypeRepository repairTypeRepository;

    public List<RepairTypeEntity> getRepairTypes() {
        return repairTypeRepository.findAllByOrderByIdAsc();
    }

    public RepairTypeEntity getRepairTypeById(Long id) {
        return repairTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de reparación no encontrado: " + id));
    }

    public long getPrice(Long id, EngineType engineType) {
        return getRepairTypeById(id).priceFor(engineType);
    }

    public RepairTypeEntity saveRepairType(RepairTypeEntity repairType) {
        repairType.setId(null);
        validate(repairType);
        if (repairTypeRepository.existsByNameIgnoreCase(repairType.getName())) {
            throw new IllegalArgumentException("Ya existe una reparación con el nombre " + repairType.getName());
        }
        return repairTypeRepository.save(repairType);
    }

    // Los precios se actualizan periódicamente: el nuevo precio rige para las reparaciones que se registren después
    public RepairTypeEntity updateRepairType(RepairTypeEntity repairType) {
        RepairTypeEntity current = getRepairTypeById(repairType.getId());
        validate(repairType);
        if (!current.getName().equalsIgnoreCase(repairType.getName())
                && repairTypeRepository.existsByNameIgnoreCase(repairType.getName())) {
            throw new IllegalArgumentException("Ya existe una reparación con el nombre " + repairType.getName());
        }
        return repairTypeRepository.save(repairType);
    }

    public boolean deleteRepairType(Long id) {
        getRepairTypeById(id);
        repairTypeRepository.deleteById(id);
        return true;
    }

    private void validate(RepairTypeEntity repairType) {
        if (repairType.getName() == null || repairType.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la reparación es obligatorio");
        }
        repairType.setName(repairType.getName().trim());
        if (repairType.getPriceGasoline() < 0 || repairType.getPriceDiesel() < 0
                || repairType.getPriceHybrid() < 0 || repairType.getPriceElectric() < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }
    }
}
