package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixRow {
    private int repairType;
    private String repairName;
    private Map<String, MatrixCell> cells;  // columna (tipo vehículo o tipo motor) -> celda
    private MatrixCell total;               // total de la fila
}
