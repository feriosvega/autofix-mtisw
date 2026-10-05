package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.dtos.MatrixReport;
import edu.mtisw.autofixbackend.dtos.MatrixRow;
import edu.mtisw.autofixbackend.dtos.ReportR1;
import edu.mtisw.autofixbackend.dtos.ReportR3Row;
import edu.mtisw.autofixbackend.entities.*;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import edu.mtisw.autofixbackend.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    RepairRepository repairRepository;

    @Mock
    VehicleRepository vehicleRepository;

    @Spy
    RepairCostService repairCostService = new RepairCostService();

    @InjectMocks
    ReportService reportService;

    private final LocalDate from = LocalDate.of(2026, 10, 1);
    private final LocalDate to = LocalDate.of(2026, 10, 31);

    private VehicleEntity sedan;
    private VehicleEntity suv;

    @BeforeEach
    void setUp() {
        sedan = new VehicleEntity(1L, "ABCD12", "Toyota", "Corolla", VehicleType.SEDAN, 2015, EngineType.GASOLINA, 5);
        suv = new VehicleEntity(2L, "WXYZ34", "Kia", "Sportage", VehicleType.SUV, 2020, EngineType.DIESEL, 5);
    }

    private RepairEntity repair(Long id, Long vehicleId, int day, int hoursToExit, long... prices) {
        RepairEntity r = new RepairEntity();
        r.setId(id);
        r.setVehicleId(vehicleId);
        r.setEntryDate(LocalDate.of(2026, 10, day));
        r.setEntryTime(LocalTime.of(8, 0));
        if (hoursToExit >= 0) {
            r.setExitDate(r.getEntryDate().plusDays(hoursToExit / 24));
            r.setExitTime(LocalTime.of(8 + hoursToExit % 24, 0));
        }
        List<RepairDetailEntity> details = new ArrayList<>();
        int type = 1;
        long sum = 0;
        for (long p : prices) {
            details.add(new RepairDetailEntity(null, type++, p));
            sum += p;
        }
        r.setDetails(details);
        r.setRepairsAmount(sum);
        r.setSubtotal(sum);
        r.setIva(Math.round(sum * 0.19));
        r.setTotalAmount(sum + r.getIva());
        return r;
    }

    @Test
    void whenReportR1_thenRowsAndTotals() {
        RepairEntity r1 = repair(1L, 1L, 5, 10, 120_000, 130_000);
        RepairEntity r2 = repair(2L, 2L, 6, 4, 120_000);
        RepairEntity orphan = repair(3L, 99L, 7, 4, 10);
        when(repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(from, to)).thenReturn(List.of(r1, r2, orphan));
        when(vehicleRepository.findAll()).thenReturn(List.of(sedan, suv));

        ReportR1 report = reportService.getReportR1(from, to);

        assertThat(report.getRows()).hasSize(2);
        assertThat(report.getRows().get(0).getPlate()).isEqualTo("ABCD12");
        assertThat(report.getTotals().getPlate()).isEqualTo("TOTAL");
        assertThat(report.getTotals().getRepairsCount()).isEqualTo(3);
        assertThat(report.getTotals().getRepairsAmount()).isEqualTo(370_000);
        assertThat(report.getTotals().getTotalAmount()).isEqualTo(r1.getTotalAmount() + r2.getTotalAmount());
    }

    @Test
    void whenInvalidRange_thenThrows() {
        assertThatThrownBy(() -> reportService.getReportR1(to, from)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportService.getReportR2(null, to)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportService.getReportR3(from, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenReportR2_thenElevenRowsSortedByAmountWithTotals() {
        // sedán: rep.1 (120.000) + rep.2 (130.000);  SUV: rep.1 (120.000)
        RepairEntity r1 = repair(1L, 1L, 5, 10, 120_000, 130_000);
        RepairEntity r2 = repair(2L, 2L, 6, 4, 120_000);
        RepairEntity orphan = repair(3L, 99L, 7, 4, 10);
        when(repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(from, to)).thenReturn(List.of(r1, r2, orphan));
        when(vehicleRepository.findAll()).thenReturn(List.of(sedan, suv));

        MatrixReport report = reportService.getReportR2(from, to);

        assertThat(report.getColumns()).containsExactly("SEDAN", "HATCHBACK", "SUV", "PICKUP", "FURGONETA");
        assertThat(report.getRows()).hasSize(11);
        MatrixRow first = report.getRows().get(0);
        assertThat(first.getRepairType()).isEqualTo(1);                 // 240.000 es el mayor monto
        assertThat(first.getCells().get("SEDAN").getCount()).isEqualTo(1);
        assertThat(first.getCells().get("SUV").getAmount()).isEqualTo(120_000);
        assertThat(first.getTotal().getAmount()).isEqualTo(240_000);
        assertThat(report.getRows().get(1).getRepairType()).isEqualTo(2);
        assertThat(report.getRows().get(2).getRepairType()).isEqualTo(3); // empates en 0: orden por número
        assertThat(report.getColumnTotals().get("SEDAN").getAmount()).isEqualTo(250_000);
        assertThat(report.getGrandTotal().getCount()).isEqualTo(3);
        assertThat(report.getGrandTotal().getAmount()).isEqualTo(370_000);
    }

    @Test
    void whenReportR4_thenColumnsAreEngineTypes() {
        RepairEntity r1 = repair(1L, 1L, 5, 10, 120_000);
        RepairEntity r2 = repair(2L, 2L, 6, 4, 120_000);
        when(repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(from, to)).thenReturn(List.of(r1, r2));
        when(vehicleRepository.findAll()).thenReturn(List.of(sedan, suv));

        MatrixReport report = reportService.getReportR4(from, to);

        assertThat(report.getColumns()).containsExactly("GASOLINA", "DIESEL", "HIBRIDO", "ELECTRICO");
        assertThat(report.getRows().get(0).getCells().get("GASOLINA").getCount()).isEqualTo(1);
        assertThat(report.getRows().get(0).getCells().get("DIESEL").getCount()).isEqualTo(1);
        assertThat(report.getColumnTotals().get("HIBRIDO").getCount()).isZero();
    }

    @Test
    void whenReportR3_thenStatsPerVehicleType() {
        RepairEntity a = repair(1L, 1L, 5, 10);
        RepairEntity b = repair(2L, 1L, 6, 4);
        RepairEntity c = repair(3L, 2L, 7, 30);  // 1 día + 6 horas
        RepairEntity unfinished = repair(4L, 1L, 8, -1);
        RepairEntity orphan = repair(5L, 99L, 8, 3);
        when(repairRepository.findByEntryDateBetweenOrderByEntryDateAsc(from, to)).thenReturn(List.of(a, b, c, unfinished, orphan));
        when(vehicleRepository.findAll()).thenReturn(List.of(sedan, suv));

        List<ReportR3Row> rows = reportService.getReportR3(from, to);

        assertThat(rows).hasSize(5);                       // siempre todos los tipos de vehículo
        ReportR3Row sedanRow = rows.get(0);
        assertThat(sedanRow.getVehicleType()).isEqualTo("SEDAN");
        assertThat(sedanRow.getRepairsCount()).isEqualTo(2);
        assertThat(sedanRow.getAverageHours()).isEqualTo(7.0);
        assertThat(sedanRow.getMinHours()).isEqualTo(4.0);
        assertThat(sedanRow.getMaxHours()).isEqualTo(10.0);
        assertThat(sedanRow.getStandardDeviationHours()).isEqualTo(3.0);
        assertThat(rows.get(2).getAverageHours()).isEqualTo(30.0);
        assertThat(rows.get(1).getRepairsCount()).isZero();
    }

    @Test
    void whenBuildR3Row_thenPercentile90NearestRank() {
        List<Double> hours = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            hours.add((double) i);
        }
        ReportR3Row row = reportService.buildR3Row("SUV", hours);
        assertThat(row.getPercentile90Hours()).isEqualTo(9.0);
        assertThat(row.getAverageHours()).isEqualTo(5.5);
    }
}
