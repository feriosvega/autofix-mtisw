package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.dtos.*;
import edu.mtisw.autofixbackend.entities.*;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import edu.mtisw.autofixbackend.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// HU4..HU7: Reportes R1, R2, R3 y R4 (todos filtrados por rango de fecha de ingreso)
@Service
public class ReportService {

    @Autowired
    RepairRepository repairRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    RepairCostService repairCostService;

    // ------------------------------------------------------------------
    // R1: listado de vehículos con todos los valores de la fórmula + totales
    // ------------------------------------------------------------------
    public ReportR1 getReportR1(LocalDate from, LocalDate to) {
        List<RepairEntity> repairs = getRepairsInRange(from, to);
        Map<Long, VehicleEntity> vehicles = getVehiclesById();

        List<ReportR1Row> rows = new ArrayList<>();
        ReportR1Row totals = new ReportR1Row();
        totals.setPlate("TOTAL");

        for (RepairEntity r : repairs) {
            VehicleEntity v = vehicles.get(r.getVehicleId());
            if (v == null) {
                continue;
            }
            ReportR1Row row = new ReportR1Row(r.getId(), v.getPlate(), v.getBrand(), v.getModel(),
                    v.getVehicleType().name(), v.getEngineType().name(), v.getManufactureYear(), r.getMileage(),
                    r.getEntryDate(), r.getExitDate(), r.getPickupDate(), r.getDetails().size(),
                    r.getRepairsAmount(), r.getMileageSurcharge(), r.getAgeSurcharge(), r.getLateSurcharge(),
                    r.getRepairsCountDiscount(), r.getDayDiscount(), r.getBonusDiscount(),
                    r.getSubtotal(), r.getIva(), r.getTotalAmount());
            rows.add(row);

            totals.setRepairsCount(totals.getRepairsCount() + row.getRepairsCount());
            totals.setRepairsAmount(totals.getRepairsAmount() + row.getRepairsAmount());
            totals.setMileageSurcharge(totals.getMileageSurcharge() + row.getMileageSurcharge());
            totals.setAgeSurcharge(totals.getAgeSurcharge() + row.getAgeSurcharge());
            totals.setLateSurcharge(totals.getLateSurcharge() + row.getLateSurcharge());
            totals.setRepairsCountDiscount(totals.getRepairsCountDiscount() + row.getRepairsCountDiscount());
            totals.setDayDiscount(totals.getDayDiscount() + row.getDayDiscount());
            totals.setBonusDiscount(totals.getBonusDiscount() + row.getBonusDiscount());
            totals.setSubtotal(totals.getSubtotal() + row.getSubtotal());
            totals.setIva(totals.getIva() + row.getIva());
            totals.setTotalAmount(totals.getTotalAmount() + row.getTotalAmount());
        }
        return new ReportR1(rows, totals);
    }

    // ------------------------------------------------------------------
    // R2: 11 reparaciones x tipo de vehículo (cantidad de vehículos y monto)
    // ------------------------------------------------------------------
    public MatrixReport getReportR2(LocalDate from, LocalDate to) {
        List<String> columns = Arrays.stream(VehicleType.values()).map(Enum::name).toList();
        return buildMatrix(from, to, columns, v -> v.getVehicleType().name());
    }

    // ------------------------------------------------------------------
    // R4: 11 reparaciones x tipo de motor (cantidad de vehículos y monto)
    // ------------------------------------------------------------------
    public MatrixReport getReportR4(LocalDate from, LocalDate to) {
        List<String> columns = Arrays.stream(EngineType.values()).map(Enum::name).toList();
        return buildMatrix(from, to, columns, v -> v.getEngineType().name());
    }

    private MatrixReport buildMatrix(LocalDate from, LocalDate to, List<String> columns,
                                     Function<VehicleEntity, String> columnOf) {
        List<RepairEntity> repairs = getRepairsInRange(from, to);
        Map<Long, VehicleEntity> vehicles = getVehiclesById();

        // Se muestran SIEMPRE las 11 reparaciones, aunque tengan 0
        List<MatrixRow> rows = new ArrayList<>();
        for (int type = 1; type <= RepairCostService.REPAIR_TYPES; type++) {
            Map<String, MatrixCell> cells = new LinkedHashMap<>();
            columns.forEach(c -> cells.put(c, new MatrixCell()));
            rows.add(new MatrixRow(type, repairCostService.getRepairName(type), cells, new MatrixCell()));
        }

        for (RepairEntity r : repairs) {
            VehicleEntity v = vehicles.get(r.getVehicleId());
            if (v == null) {
                continue;
            }
            String column = columnOf.apply(v);
            for (RepairDetailEntity d : r.getDetails()) {
                MatrixRow row = rows.get(d.getRepairType() - 1);
                row.getCells().get(column).add(1, d.getPrice());
                row.getTotal().add(1, d.getPrice());
            }
        }

        // Totales por columna y total general
        Map<String, MatrixCell> columnTotals = new LinkedHashMap<>();
        columns.forEach(c -> columnTotals.put(c, new MatrixCell()));
        MatrixCell grandTotal = new MatrixCell();
        for (MatrixRow row : rows) {
            row.getCells().forEach((c, cell) -> columnTotals.get(c).add(cell.getCount(), cell.getAmount()));
            grandTotal.add(row.getTotal().getCount(), row.getTotal().getAmount());
        }

        // Ordenar de mayor a menor por monto
        rows.sort(Comparator.comparingLong((MatrixRow row) -> row.getTotal().getAmount()).reversed()
                .thenComparingInt(MatrixRow::getRepairType));
        return new MatrixReport(columns, rows, columnTotals, grandTotal);
    }

    // ------------------------------------------------------------------
    // R3: tiempos de reparación (ingreso -> salida) por tipo de vehículo
    // ------------------------------------------------------------------
    public List<ReportR3Row> getReportR3(LocalDate from, LocalDate to) {
        List<RepairEntity> repairs = getRepairsInRange(from, to);
        Map<Long, VehicleEntity> vehicles = getVehiclesById();

        Map<VehicleType, List<Double>> hoursByType = new EnumMap<>(VehicleType.class);
        for (VehicleType t : VehicleType.values()) {
            hoursByType.put(t, new ArrayList<>());
        }
        for (RepairEntity r : repairs) {
            VehicleEntity v = vehicles.get(r.getVehicleId());
            if (v == null || r.getExitDate() == null || r.getExitTime() == null) {
                continue; // solo reparaciones terminadas tienen tiempo de reparación
            }
            LocalDateTime entry = LocalDateTime.of(r.getEntryDate(), r.getEntryTime());
            LocalDateTime exit = LocalDateTime.of(r.getExitDate(), r.getExitTime());
            hoursByType.get(v.getVehicleType()).add(Duration.between(entry, exit).toMinutes() / 60.0);
        }

        List<ReportR3Row> rows = new ArrayList<>();
        hoursByType.forEach((type, hours) -> rows.add(buildR3Row(type.name(), hours)));
        return rows;
    }

    public ReportR3Row buildR3Row(String vehicleType, List<Double> hours) {
        if (hours.isEmpty()) {
            return new ReportR3Row(vehicleType, 0, 0, 0, 0, 0, 0);
        }
        List<Double> sorted = new ArrayList<>(hours);
        Collections.sort(sorted);
        int n = sorted.size();
        double avg = sorted.stream().mapToDouble(Double::doubleValue).sum() / n;
        double variance = sorted.stream().mapToDouble(h -> (h - avg) * (h - avg)).sum() / n; // poblacional
        double std = Math.sqrt(variance);
        // Percentil 90 por método "nearest rank": el valor en la posición ceil(0.9 * n)
        int p90Index = (int) Math.ceil(0.9 * n) - 1;
        return new ReportR3Row(vehicleType, n, round2(avg), round2(std), round2(sorted.get(0)),
                round2(sorted.get(n - 1)), round2(sorted.get(p90Index)));
    }

    // ------------------------------------------------------------------
    private List<RepairEntity> getRepairsInRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        return repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(from, to);
    }

    private Map<Long, VehicleEntity> getVehiclesById() {
        return vehicleRepository.findAll().stream().collect(Collectors.toMap(VehicleEntity::getId, v -> v));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
