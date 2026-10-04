package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// R1: una fila por vehículo/ingreso con todos los valores de la fórmula de costo total
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportR1Row {
    private Long repairId;
    private String plate;
    private String brand;
    private String model;
    private String vehicleType;
    private String engineType;
    private int manufactureYear;
    private int mileage;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private LocalDate pickupDate;
    private int repairsCount;
    private long repairsAmount;
    private long mileageSurcharge;
    private long ageSurcharge;
    private long lateSurcharge;
    private long repairsCountDiscount;
    private long dayDiscount;
    private long bonusDiscount;
    private long subtotal;
    private long iva;
    private long totalAmount;
}
