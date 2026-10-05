package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.EngineType;
import edu.mtisw.autofixbackend.entities.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepairCostServiceTest {

    private RepairCostService costService;

    @BeforeEach
    void setUp() {
        costService = new RepairCostService();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Tabla de precios
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @ParameterizedTest
    @CsvSource({
            "1, GASOLINA, 120000", "1, ELECTRICO, 220000",
            "2, GASOLINA, 130000", "3, DIESEL, 450000",
            "3, HIBRIDO, 700000", "6, ELECTRICO, 0",
            "10, DIESEL, 140000", "11, HIBRIDO, 80000"
    })
    void whenGetRepairPrice_thenReturnsTablePrice(int type, EngineType engine, long expected) {
        assertThat(costService.getRepairPrice(type, engine)).isEqualTo(expected);
    }

    @Test
    void whenRepairTypeOutOfRange_thenThrows() {
        assertThatThrownBy(() -> costService.getRepairPrice(0, EngineType.GASOLINA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> costService.getRepairName(12))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenElectricExhaust_thenRepairNotAvailable() {
        assertThat(costService.isRepairAvailable(6, EngineType.ELECTRICO)).isFalse();
        assertThat(costService.isRepairAvailable(6, EngineType.GASOLINA)).isTrue();
    }

    @Test
    void whenGetRepairName_thenReturnsName() {
        assertThat(costService.getRepairName(1)).isEqualTo("Reparaciones del Sistema de Frenos");
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Descuento por número de reparaciones (últimos 12 meses)
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @ParameterizedTest
    @CsvSource({
            "0, GASOLINA, 0.0",
            "1, GASOLINA, 0.05", "2, DIESEL, 0.07",
            "3, HIBRIDO, 0.15", "5, ELECTRICO, 0.13",
            "6, GASOLINA, 0.15", "9, DIESEL, 0.17",
            "10, HIBRIDO, 0.25", "25, ELECTRICO, 0.23"
    })
    void whenGetRepairsCountDiscountRate_thenTableRate(long count, EngineType engine, double expected) {
        assertThat(costService.getRepairsCountDiscountRate(count, engine)).isEqualTo(expected);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Descuento por día de atención (lunes y jueves 09:00 - 12:00)
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Test
    void whenMondayAt10_thenDayDiscount10Percent() {
        // 2026-10-05 es lunes
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 5), LocalTime.of(10, 0))).isEqualTo(0.10);
    }

    @Test
    void whenThursdayAtLimits_thenDayDiscount() {
        // 2026-10-08 es jueves
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 8), LocalTime.of(9, 0))).isEqualTo(0.10);
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 8), LocalTime.of(12, 0))).isEqualTo(0.10);
    }

    @Test
    void whenMondayOutOfHours_thenNoDayDiscount() {
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 5), LocalTime.of(8, 59))).isZero();
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 5), LocalTime.of(12, 1))).isZero();
    }

    @Test
    void whenTuesday_thenNoDayDiscount() {
        assertThat(costService.getDayDiscountRate(LocalDate.of(2026, 10, 6), LocalTime.of(10, 0))).isZero();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recargo por kilometraje
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @ParameterizedTest
    @CsvSource({
            "5000, SEDAN, 0.0", "5001, HATCHBACK, 0.03", "12000, SUV, 0.05",
            "12001, SEDAN, 0.07", "25000, PICKUP, 0.09", "25001, FURGONETA, 0.12",
            "40000, SEDAN, 0.12", "40001, SUV, 0.20", "150000, HATCHBACK, 0.20"
    })
    void whenGetMileageSurchargeRate_thenTableRate(int km, VehicleType type, double expected) {
        assertThat(costService.getMileageSurchargeRate(km, type)).isEqualTo(expected);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recargo por antigüedad
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @ParameterizedTest
    @CsvSource({
            "0, SEDAN, 0.0", "5, SUV, 0.0", "6, HATCHBACK, 0.05", "10, PICKUP, 0.07",
            "11, SEDAN, 0.09", "15, FURGONETA, 0.11", "16, HATCHBACK, 0.15", "30, SUV, 0.20"
    })
    void whenGetAgeSurchargeRate_thenTableRate(int age, VehicleType type, double expected) {
        assertThat(costService.getAgeSurchargeRate(age, type)).isEqualTo(expected);
    }

    @Test
    void whenGetVehicleAge_thenEntryYearMinusManufactureYear() {
        assertThat(costService.getVehicleAge(2015, LocalDate.of(2026, 1, 1))).isEqualTo(11);
        assertThat(costService.getVehicleAge(2027, LocalDate.of(2026, 1, 1))).isZero();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Recargo por retraso en la recogida (5% por día)
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Test
    void whenPickupTwoDaysLate_thenSurcharge10Percent() {
        double rate = costService.getLateSurchargeRate(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 9));
        assertThat(rate).isEqualTo(0.10, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void whenPickupSameDayOrMissingDates_thenNoSurcharge() {
        assertThat(costService.getLateSurchargeRate(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 7))).isZero();
        assertThat(costService.getLateSurchargeRate(null, LocalDate.of(2026, 10, 7))).isZero();
        assertThat(costService.getLateSurchargeRate(LocalDate.of(2026, 10, 7), null)).isZero();
    }

    @Test
    void whenApplyRate_thenRounds() {
        assertThat(costService.applyRate(270_000, 0.07)).isEqualTo(18_900);
        assertThat(costService.applyRate(101, 0.5)).isEqualTo(51);
    }
}
