package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// R3: estadísticas del tiempo de reparación (en horas) por tipo de vehículo
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportR3Row {
    private String vehicleType;
    private int repairsCount;
    private double averageHours;
    private double standardDeviationHours;
    private double minHours;
    private double maxHours;
    private double percentile90Hours;
}
