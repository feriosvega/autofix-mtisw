package edu.mtisw.autofixbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Celda de R2/R4: cantidad de vehículos reparados y monto total
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixCell {
    private long count;
    private long amount;

    public void add(long count, long amount) {
        this.count += count;
        this.amount += amount;
    }
}
