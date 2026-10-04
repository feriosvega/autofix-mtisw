package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// R2 (reparación x tipo de vehículo) y R4 (reparación x tipo de motor)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixReport {
    private List<String> columns;
    private List<MatrixRow> rows;               // 11 filas, ordenadas de mayor a menor monto
    private Map<String, MatrixCell> columnTotals; // totales al final de cada columna
    private MatrixCell grandTotal;
}
