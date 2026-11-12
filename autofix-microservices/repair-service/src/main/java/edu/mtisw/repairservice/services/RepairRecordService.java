package edu.mtisw.repairservice.services;

import edu.mtisw.repairservice.dtos.RepairReportDataDto;
import edu.mtisw.repairservice.dtos.RepairReportDetailDto;
import edu.mtisw.repairservice.dtos.VehicleHistoryDto;
import edu.mtisw.repairservice.entities.RepairDetailEntity;
import edu.mtisw.repairservice.entities.RepairRecordEntity;
import edu.mtisw.repairservice.entities.VehicleInfo;
import edu.mtisw.repairservice.repositories.RepairRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RepairRecordService {
    private static final String VEHICLES = "http://vehicle-service/api/v1/vehicles/";
    private static final String REPAIR_TYPES = "http://repair-list-service/api/v1/repair-types/";

    @Autowired private RepairRecordRepository repository;
    @Autowired private RestTemplate restTemplate;
    @Autowired private BonusService bonusService;

    public List<RepairRecordEntity> findAll() {
        return repository.findAll();
    }

    public RepairRecordEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reparación no encontrada: " + id));
    }

    public List<RepairRecordEntity> findByVehicle(Long vehicleId) {
        return repository.findByVehicleIdOrderByEntryDateDescEntryTimeDesc(vehicleId);
    }

    @Transactional
    public RepairRecordEntity create(RepairRecordEntity record) {
        record.setId(null);
        return saveAndCalculate(record);
    }

    @Transactional
    public RepairRecordEntity update(RepairRecordEntity record) {
        findById(record.getId());
        return saveAndCalculate(record);
    }

    @Transactional
    public RepairRecordEntity calculate(Long id) {
        return saveAndCalculate(findById(id));
    }

    @Transactional
    public void delete(Long id) {
        RepairRecordEntity record = findById(id);
        if (record.getBonusDiscount() > 0) {
            VehicleInfo vehicle = restTemplate.getForObject(VEHICLES + record.getVehicleId(), VehicleInfo.class);
            if (vehicle != null) bonusService.returnBonus(vehicle.getBrand(), record.getEntryDate());
        }
        repository.deleteById(id);
    }

    public boolean vehicleHasRepairs(Long vehicleId) {
        return repository.existsByVehicleId(vehicleId);
    }

    public List<VehicleHistoryDto> history() {
        List<VehicleInfo> vehicles = restTemplate.exchange(VEHICLES, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<VehicleInfo>>() {}).getBody();
        if (vehicles == null || vehicles.isEmpty()) return List.of();
        Map<Long, List<RepairRecordEntity>> repairsByVehicle = repository.findAll().stream()
                .collect(Collectors.groupingBy(RepairRecordEntity::getVehicleId));
        return vehicles.stream()
                .map(vehicle -> new VehicleHistoryDto(vehicle,
                        repairsByVehicle.getOrDefault(vehicle.getId(), List.of())))
                .toList();
    }

    public List<RepairReportDataDto> reportData() {
        return history().stream().flatMap(history -> history.getRepairs().stream().map(repair ->
                new RepairReportDataDto(repair.getId(), history.getVehicle().getId(),
                        history.getVehicle().getPlate(), history.getVehicle().getVehicleType(),
                        repair.getDetails().stream().map(detail -> new RepairReportDetailDto(
                                detail.getRepairType(), detail.getRepairName(), detail.getRepairDate(),
                                detail.getRepairTime(), detail.getPrice())).toList())))
                .toList();
    }

    private RepairRecordEntity saveAndCalculate(RepairRecordEntity record) {
        validate(record);
        // Bono que ya tenía descontado este ingreso (para no consumirlo dos veces o devolverlo si se quita)
        long previousBonus = record.getId() == null ? 0
                : repository.findById(record.getId()).map(RepairRecordEntity::getBonusDiscount).orElse(0L);
        VehicleInfo vehicle = restTemplate.getForObject(VEHICLES + record.getVehicleId(), VehicleInfo.class);
        if (vehicle == null) throw new EntityNotFoundException("Vehículo no encontrado: " + record.getVehicleId());

        String engineType = vehicle.getEngineType();
        List<RepairDetailEntity> details = new ArrayList<>();
        for (RepairDetailEntity detail : record.getDetails()) {
            if (detail.getRepairType() == null) throw new IllegalArgumentException("Debe seleccionar un tipo de reparación");
            RepairTypeInfo repairType = restTemplate.getForObject(REPAIR_TYPES + detail.getRepairType(), RepairTypeInfo.class);
            if (repairType == null) throw new EntityNotFoundException("Tipo de reparación no encontrado: " + detail.getRepairType());
            long price = restTemplate.getForObject(REPAIR_TYPES + detail.getRepairType() + "/price/" + engineType, Long.class);
            if (price <= 0) throw new IllegalArgumentException("La reparación " + detail.getRepairType() + " no aplica al motor " + engineType);
            detail.setId(null);
            detail.setRepairName(repairType.getName());
            detail.setPrice(price);
            if (detail.getRepairDate() == null) detail.setRepairDate(record.getEntryDate());
            if (detail.getRepairTime() == null) detail.setRepairTime(record.getEntryTime());
            details.add(detail);
        }
        record.setDetails(details);

        long repairsAmount = details.stream().mapToLong(RepairDetailEntity::getPrice).sum();
        record.setRepairsAmount(repairsAmount);
        // El Costo Total se calcula cuando el cliente retira el vehículo (enunciado)
        if (record.getPickupDate() == null) {
            if (previousBonus > 0) bonusService.returnBonus(vehicle.getBrand(), record.getEntryDate());
            record.setMileageSurcharge(0);
            record.setAgeSurcharge(0);
            record.setLateSurcharge(0);
            record.setRepairsCountDiscount(0);
            record.setDayDiscount(0);
            record.setBonusDiscount(0);
            record.setSubtotal(0);
            record.setIva(0);
            record.setTotalAmount(0);
            return repository.save(record);
        }
        long priorCount = repository.countByVehicleIdAndEntryDateBetweenAndIdNot(
                record.getVehicleId(), record.getEntryDate().minusMonths(12), record.getEntryDate(),
                record.getId() == null ? -1L : record.getId());

        double mileageRate = mileageRate(record.getMileage(), vehicle.getVehicleType());
        double ageRate = ageRate(Math.max(0, record.getEntryDate().getYear() - vehicle.getManufactureYear()),
                vehicle.getVehicleType());
        long lateDays = record.getExitDate() == null || record.getPickupDate() == null
                ? 0 : Math.max(0, ChronoUnit.DAYS.between(record.getExitDate(), record.getPickupDate()));
        long mileageSurcharge = applyRate(repairsAmount, mileageRate);
        long ageSurcharge = applyRate(repairsAmount, ageRate);
        long lateSurcharge = applyRate(repairsAmount, 0.05 * lateDays);
        long countDiscount = applyRate(repairsAmount, countDiscountRate(priorCount + 1, engineType));
        DayOfWeek day = record.getEntryDate().getDayOfWeek();
        LocalTime time = record.getEntryTime();
        boolean discountedDay = (day == DayOfWeek.MONDAY || day == DayOfWeek.THURSDAY)
                && !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(12, 0));
        long dayDiscount = discountedDay ? applyRate(repairsAmount, 0.10) : 0;
        long bonusDiscount = resolveBonus(record, vehicle, previousBonus);
        long subtotal = Math.max(0, repairsAmount + mileageSurcharge + ageSurcharge + lateSurcharge
                - countDiscount - dayDiscount - bonusDiscount);

        record.setRepairsAmount(repairsAmount);
        record.setMileageSurcharge(mileageSurcharge);
        record.setAgeSurcharge(ageSurcharge);
        record.setLateSurcharge(lateSurcharge);
        record.setRepairsCountDiscount(countDiscount);
        record.setDayDiscount(dayDiscount);
        record.setBonusDiscount(bonusDiscount);
        record.setSubtotal(subtotal);
        record.setIva(applyRate(subtotal, 0.19));
        record.setTotalAmount(subtotal + record.getIva());
        return repository.save(record);
    }

    // Descuento por bono: monto fijo, uno por vehículo (por mes), solo marcas con convenio TopCar
    private long resolveBonus(RepairRecordEntity record, VehicleInfo vehicle, long previousBonus) {
        if (!record.isApplyBonus()) {
            if (previousBonus > 0) bonusService.returnBonus(vehicle.getBrand(), record.getEntryDate());
            return 0;
        }
        if (previousBonus > 0) return previousBonus;
        LocalDate firstDay = record.getEntryDate().withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
        long used = repository.countBonusesUsed(record.getVehicleId(), firstDay, lastDay,
                record.getId() == null ? -1L : record.getId());
        if (used > 0) throw new IllegalStateException("Solo se puede aplicar un bono por vehículo (ya usó un bono este mes)");
        return bonusService.consumeBonus(vehicle.getBrand(), record.getEntryDate());
    }

    private void validate(RepairRecordEntity record) {
        if (record.getVehicleId() == null) throw new IllegalArgumentException("Debe seleccionar un vehículo");
        if (record.getEntryDate() == null || record.getEntryTime() == null)
            throw new IllegalArgumentException("Fecha y hora de ingreso son obligatorias");
        if (record.getMileage() < 0) throw new IllegalArgumentException("El kilometraje no puede ser negativo");
        if (record.getDetails() == null || record.getDetails().isEmpty())
            throw new IllegalArgumentException("Debe registrar al menos una reparación");
        Set<Long> repairTypes = new HashSet<>();
        for (RepairDetailEntity detail : record.getDetails()) {
            if (detail.getRepairType() == null) throw new IllegalArgumentException("Debe seleccionar un tipo de reparación");
            if (!repairTypes.add(detail.getRepairType())) throw new IllegalArgumentException("No repita tipos de reparación en el mismo ingreso");
        }
        LocalDate readyDate = record.getExitDate();
        LocalDate pickupDate = record.getPickupDate();
        if (readyDate != null && (readyDate.isBefore(record.getEntryDate())
                || (readyDate.equals(record.getEntryDate()) && record.getExitTime() != null
                && record.getExitTime().isBefore(record.getEntryTime()))))
            throw new IllegalArgumentException("La fecha de salida no puede ser anterior al ingreso");
        if (pickupDate != null && readyDate == null)
            throw new IllegalArgumentException("Registre la fecha de salida antes del retiro");
        if (pickupDate != null && (pickupDate.isBefore(readyDate)
                || (pickupDate.equals(readyDate) && record.getPickupTime() != null && record.getExitTime() != null
                && record.getPickupTime().isBefore(record.getExitTime()))))
            throw new IllegalArgumentException("La fecha/hora de retiro no puede ser anterior a la salida");
    }

    private long applyRate(long amount, double rate) { return Math.round(amount * rate); }

    private double countDiscountRate(long count, String engine) {
        if (count == 0) return 0;
        int row = count <= 2 ? 0 : count <= 5 ? 1 : count <= 9 ? 2 : 3;
        double[][] rates = {{.05,.07,.10,.08},{.10,.12,.15,.13},{.15,.17,.20,.18},{.20,.22,.25,.23}};
        int column = switch (engine) {
            case "GASOLINA" -> 0; case "DIESEL" -> 1; case "HIBRIDO" -> 2; case "ELECTRICO" -> 3;
            default -> throw new IllegalArgumentException("Tipo de motor desconocido: " + engine);
        };
        return rates[row][column];
    }

    private double mileageRate(int mileage, String vehicleType) {
        boolean light = "SEDAN".equals(vehicleType) || "HATCHBACK".equals(vehicleType);
        if (mileage <= 5000) return 0;
        if (mileage <= 12000) return light ? .03 : .05;
        if (mileage <= 25000) return light ? .07 : .09;
        if (mileage <= 40000) return .12;
        return .20;
    }

    private double ageRate(int age, String vehicleType) {
        boolean light = "SEDAN".equals(vehicleType) || "HATCHBACK".equals(vehicleType);
        if (age <= 5) return 0;
        if (age <= 10) return light ? .05 : .07;
        if (age <= 15) return light ? .09 : .11;
        return light ? .15 : .20;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    private static class RepairTypeInfo { private Long id; private String name; }
}
