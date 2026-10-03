package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.RepairDetailEntity;
import edu.mtisw.autofixbackend.entities.RepairEntity;
import edu.mtisw.autofixbackend.entities.VehicleEntity;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// HU2: Registro de reparaciones  +  HU3: Cálculo del costo total
@Service
public class RepairService {

    @Autowired
    RepairRepository repairRepository;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    BonusService bonusService;

    @Autowired
    RepairCostService repairCostService;

    public List<RepairEntity> getRepairs() {
        return repairRepository.findAll();
    }

    public RepairEntity getRepairById(Long id) {
        return repairRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reparación no encontrada: " + id));
    }

    public List<RepairEntity> getRepairsByVehicle(Long vehicleId) {
        return repairRepository.findByVehicleIdOrderByEntryDateDesc(vehicleId);
    }

    // HU2: registra el ingreso con sus reparaciones y calcula el costo (HU3)
    @Transactional
    public RepairEntity saveRepair(RepairEntity repair) {
        repair.setId(null);
        VehicleEntity vehicle = vehicleService.getVehicleById(repair.getVehicleId());
        validate(repair, vehicle);

        long bonusAmount = 0;
        if (repair.isApplyBonus()) {
            checkOneBonusPerVehicle(repair, vehicle);
            bonusAmount = bonusService.consumeBonus(vehicle.getBrand(), repair.getEntryDate());
        }
        calculateCost(repair, vehicle, bonusAmount);
        return repairRepository.save(repair);
    }

    // Permite completar fecha/hora de salida y de retiro, o corregir datos. Recalcula el costo.
    @Transactional
    public RepairEntity updateRepair(RepairEntity repair) {
        RepairEntity current = getRepairById(repair.getId());
        VehicleEntity vehicle = vehicleService.getVehicleById(repair.getVehicleId());
        validate(repair, vehicle);

        boolean hadBonus = current.getBonusDiscount() > 0;
        long bonusAmount = current.getBonusDiscount();
        if (hadBonus && !repair.isApplyBonus()) {
            bonusService.returnBonus(vehicle.getBrand(), current.getEntryDate());
            bonusAmount = 0;
        } else if (!hadBonus && repair.isApplyBonus()) {
            checkOneBonusPerVehicle(repair, vehicle);
            bonusAmount = bonusService.consumeBonus(vehicle.getBrand(), repair.getEntryDate());
        }
        calculateCost(repair, vehicle, bonusAmount);
        return repairRepository.save(repair);
    }

    // HU3: recalcula y guarda el costo total de una reparación existente
    @Transactional
    public RepairEntity calculateRepair(Long id) {
        RepairEntity repair = getRepairById(id);
        VehicleEntity vehicle = vehicleService.getVehicleById(repair.getVehicleId());
        calculateCost(repair, vehicle, repair.getBonusDiscount());
        return repairRepository.save(repair);
    }

    @Transactional
    public boolean deleteRepair(Long id) {
        RepairEntity repair = getRepairById(id);
        if (repair.getBonusDiscount() > 0) {
            VehicleEntity vehicle = vehicleService.getVehicleById(repair.getVehicleId());
            bonusService.returnBonus(vehicle.getBrand(), repair.getEntryDate());
        }
        repairRepository.deleteById(id);
        return true;
    }

    /**
     * HU3 - Costo Total = [Suma(Reparaciones) + Recargos - Descuentos] + IVA
     * Todos los porcentajes se aplican sobre la Suma(Reparaciones). El bono es un monto fijo.
     */
    public RepairEntity calculateCost(RepairEntity repair, VehicleEntity vehicle, long bonusAmount) {
        // 1) Suma de reparaciones según el tipo de motor
        long repairsAmount = 0;
        for (RepairDetailEntity detail : repair.getDetails()) {
            long price = repairCostService.getRepairPrice(detail.getRepairType(), vehicle.getEngineType());
            detail.setPrice(price);
            repairsAmount += price;
        }

        // 2) Recargos
        double mileageRate = repairCostService.getMileageSurchargeRate(repair.getMileage(), vehicle.getVehicleType());
        int age = repairCostService.getVehicleAge(vehicle.getManufactureYear(), repair.getEntryDate());
        double ageRate = repairCostService.getAgeSurchargeRate(age, vehicle.getVehicleType());
        double lateRate = repairCostService.getLateSurchargeRate(repair.getExitDate(), repair.getPickupDate());

        // 3) Descuentos
        long previousRepairs = repairRepository.countRepairsInPeriod(vehicle.getId(),
                repair.getEntryDate().minusMonths(12), repair.getEntryDate(), idOrNone(repair));
        double countRate = repairCostService.getRepairsCountDiscountRate(previousRepairs, vehicle.getEngineType());
        double dayRate = repairCostService.getDayDiscountRate(repair.getEntryDate(), repair.getEntryTime());

        repair.setRepairsAmount(repairsAmount);
        repair.setMileageSurcharge(repairCostService.applyRate(repairsAmount, mileageRate));
        repair.setAgeSurcharge(repairCostService.applyRate(repairsAmount, ageRate));
        repair.setLateSurcharge(repairCostService.applyRate(repairsAmount, lateRate));
        repair.setRepairsCountDiscount(repairCostService.applyRate(repairsAmount, countRate));
        repair.setDayDiscount(repairCostService.applyRate(repairsAmount, dayRate));
        repair.setBonusDiscount(bonusAmount);

        long surcharges = repair.getMileageSurcharge() + repair.getAgeSurcharge() + repair.getLateSurcharge();
        long discounts = repair.getRepairsCountDiscount() + repair.getDayDiscount() + repair.getBonusDiscount();
        long subtotal = Math.max(0, repairsAmount + surcharges - discounts);

        repair.setSubtotal(subtotal);
        repair.setIva(repairCostService.applyRate(subtotal, RepairCostService.IVA_RATE));
        repair.setTotalAmount(subtotal + repair.getIva());
        return repair;
    }

    private void checkOneBonusPerVehicle(RepairEntity repair, VehicleEntity vehicle) {
        LocalDate firstDay = repair.getEntryDate().withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
        long used = repairRepository.countBonusesUsed(vehicle.getId(), firstDay, lastDay, idOrNone(repair));
        if (used > 0) {
            throw new IllegalStateException("Solo se puede aplicar un bono por vehículo (ya usó un bono este mes)");
        }
    }

    // Para excluir la propia reparación de los conteos (-1 si aún no existe en BD)
    private Long idOrNone(RepairEntity repair) {
        return repair.getId() == null ? -1L : repair.getId();
    }

    private void validate(RepairEntity repair, VehicleEntity vehicle) {
        if (repair.getEntryDate() == null || repair.getEntryTime() == null) {
            throw new IllegalArgumentException("Fecha y hora de ingreso son obligatorias");
        }
        if (repair.getDetails() == null || repair.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una reparación");
        }
        if (repair.getMileage() < 0) {
            throw new IllegalArgumentException("El kilometraje no puede ser negativo");
        }
        // Sin reparaciones repetidas dentro del mismo ingreso
        Set<Integer> types = new LinkedHashSet<>();
        List<RepairDetailEntity> cleanDetails = new ArrayList<>();
        for (RepairDetailEntity detail : repair.getDetails()) {
            if (!repairCostService.isRepairAvailable(detail.getRepairType(), vehicle.getEngineType())) {
                throw new IllegalArgumentException("La reparación " + detail.getRepairType()
                        + " no aplica para motor " + vehicle.getEngineType());
            }
            if (types.add(detail.getRepairType())) {
                cleanDetails.add(detail);
            }
        }
        repair.setDetails(cleanDetails);

        LocalDateTime entry = LocalDateTime.of(repair.getEntryDate(), repair.getEntryTime());
        LocalDateTime exit = toDateTime(repair.getExitDate(), repair.getExitTime());
        LocalDateTime pickup = toDateTime(repair.getPickupDate(), repair.getPickupTime());
        if (exit != null && exit.isBefore(entry)) {
            throw new IllegalArgumentException("La salida de la reparación no puede ser anterior al ingreso");
        }
        if (pickup != null && (exit == null || pickup.isBefore(exit))) {
            throw new IllegalArgumentException("El retiro debe ser posterior a la salida de la reparación");
        }
    }

    private LocalDateTime toDateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, time == null ? LocalTime.MIDNIGHT : time);
    }
}
