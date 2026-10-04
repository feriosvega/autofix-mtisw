package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportR1 {
    private List<ReportR1Row> rows;
    private ReportR1Row totals;     // Totales al final de todas las filas
}
