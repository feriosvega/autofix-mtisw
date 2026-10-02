package edu.mtisw.autofixbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una de las 11 reparaciones realizadas dentro de un ingreso al taller, con el precio aplicado
@Entity
@Table(name = "repair_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private int repairType;         // 1..11 (ver RepairCostService.REPAIR_NAMES)
    private long price;             // Precio según tipo de motor al momento del cálculo
}
