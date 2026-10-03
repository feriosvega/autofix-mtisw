package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.EngineType;
import edu.mtisw.autofixbackend.entities.VehicleType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

// Reglas de negocio de AutoFix (tablas del enunciado). No accede a la BD: es lógica pura y 100% testeable.
@Service
public class RepairCostService {

    public static final double IVA_RATE = 0.19;
    public static final int REPAIR_TYPES = 11;

    // Índice = número de reparación (1..11). La posición 0 no se usa.
    public static final String[] REPAIR_NAMES = {
            "",
            "Reparaciones del Sistema de Frenos",
            "Servicio del Sistema de Refrigeración",
            "Reparaciones del Motor",
            "Reparaciones de la Transmisión",
            "Reparación del Sistema Eléctrico",
            "Reparaciones del Sistema de Escape",
            "Reparación de Neumáticos y Ruedas",
            "Reparaciones de la Suspensión y la Dirección",
            "Reparación del Sistema de Aire Acondicionado y Calefacción",
            "Reparaciones del Sistema de Combustible",
            "Reparación y Reemplazo del Parabrisas y Cristales"
    };

    // Precios por reparación. Columnas en el orden de EngineType: GASOLINA, DIESEL, HIBRIDO, ELECTRICO.
    // Nota: el enunciado muestra "130.00" para la reparación 2 Gasolina; se interpreta como 130.000 (error de tipeo).
    private static final long[][] PRICES = {
            {0, 0, 0, 0},
            {120_000, 120_000, 180_000, 220_000},
            {130_000, 130_000, 190_000, 230_000},
            {350_000, 450_000, 700_000, 800_000},
            {210_000, 210_000, 300_000, 300_000},
            {150_000, 150_000, 200_000, 250_000},
            {100_000, 120_000, 450_000, 0},
            {100_000, 100_000, 100_000, 100_000},
            {180_000, 180_000, 210_000, 250_000},
            {150_000, 150_000, 180_000, 180_000},
            {130_000, 140_000, 220_000, 0},
            {80_000, 80_000, 80_000, 80_000}
    };

    // Descuento por nro. de reparaciones en los últimos 12 meses. Filas: 1-2, 3-5, 6-9, 10+
    private static final double[][] REPAIRS_COUNT_DISCOUNT = {
            {0.05, 0.07, 0.10, 0.08},
            {0.10, 0.12, 0.15, 0.13},
            {0.15, 0.17, 0.20, 0.18},
            {0.20, 0.22, 0.25, 0.23}
    };

    // ------------------------------------------------------------------
    // Precios
    // ------------------------------------------------------------------
    public long getRepairPrice(int repairType, EngineType engineType) {
        validateRepairType(repairType);
        return PRICES[repairType][engineType.ordinal()];
    }

    // Precio 0 en la tabla = la reparación no aplica (ej. sistema de escape en un eléctrico)
    public boolean isRepairAvailable(int repairType, EngineType engineType) {
        return getRepairPrice(repairType, engineType) > 0;
    }

    public String getRepairName(int repairType) {
        validateRepairType(repairType);
        return REPAIR_NAMES[repairType];
    }

    private void validateRepairType(int repairType) {
        if (repairType < 1 || repairType > REPAIR_TYPES) {
            throw new IllegalArgumentException("Tipo de reparación inválido: " + repairType + " (debe ser 1..11)");
        }
    }

    // ------------------------------------------------------------------
    // Descuentos
    // ------------------------------------------------------------------
    public double getRepairsCountDiscountRate(long previousRepairs, EngineType engineType) {
        int row;
        if (previousRepairs <= 0) {
            return 0;
        } else if (previousRepairs <= 2) {
            row = 0;
        } else if (previousRepairs <= 5) {
            row = 1;
        } else if (previousRepairs <= 9) {
            row = 2;
        } else {
            row = 3;
        }
        return REPAIRS_COUNT_DISCOUNT[row][engineType.ordinal()];
    }

    // 10% si ingresa lunes o jueves entre 09:00 y 12:00 (ambos inclusive)
    public double getDayDiscountRate(LocalDate entryDate, LocalTime entryTime) {
        DayOfWeek day = entryDate.getDayOfWeek();
        boolean validDay = day == DayOfWeek.MONDAY || day == DayOfWeek.THURSDAY;
        boolean validHour = !entryTime.isBefore(LocalTime.of(9, 0)) && !entryTime.isAfter(LocalTime.of(12, 0));
        return (validDay && validHour) ? 0.10 : 0;
    }

    // ------------------------------------------------------------------
    // Recargos
    // ------------------------------------------------------------------
    public double getMileageSurchargeRate(int mileage, VehicleType vehicleType) {
        boolean lightVehicle = vehicleType == VehicleType.SEDAN || vehicleType == VehicleType.HATCHBACK;
        if (mileage <= 5_000) {
            return 0;
        } else if (mileage <= 12_000) {
            return lightVehicle ? 0.03 : 0.05;
        } else if (mileage <= 25_000) {
            return lightVehicle ? 0.07 : 0.09;
        } else if (mileage <= 40_000) {
            return 0.12;
        } else {
            return 0.20;
        }
    }

    public double getAgeSurchargeRate(int vehicleAge, VehicleType vehicleType) {
        boolean lightVehicle = vehicleType == VehicleType.SEDAN || vehicleType == VehicleType.HATCHBACK;
        if (vehicleAge <= 5) {
            return 0;
        } else if (vehicleAge <= 10) {
            return lightVehicle ? 0.05 : 0.07;
        } else if (vehicleAge <= 15) {
            return lightVehicle ? 0.09 : 0.11;
        } else {
            return lightVehicle ? 0.15 : 0.20;
        }
    }

    // Antigüedad = año de ingreso al taller - año de fabricación
    public int getVehicleAge(int manufactureYear, LocalDate entryDate) {
        return Math.max(0, entryDate.getYear() - manufactureYear);
    }

    // 5% por cada día de retraso, contando desde que el vehículo está listo (fecha de salida de la reparación)
    public double getLateSurchargeRate(LocalDate exitDate, LocalDate pickupDate) {
        if (exitDate == null || pickupDate == null) {
            return 0;
        }
        long lateDays = ChronoUnit.DAYS.between(exitDate, pickupDate);
        return lateDays > 0 ? 0.05 * lateDays : 0;
    }

    // ------------------------------------------------------------------
    // Utilitario
    // ------------------------------------------------------------------
    public long applyRate(long amount, double rate) {
        return Math.round(amount * rate);
    }
}
