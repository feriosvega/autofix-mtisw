package edu.mtisw.autofixbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// HU1: Vehículo que llega por primera vez al taller
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String plate;           // Patente: 4 letras + 2 números (ej. ABCD12)

    private String brand;           // Marca
    private String model;           // Modelo

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private int manufactureYear;    // Año de fabricación

    @Enumerated(EnumType.STRING)
    private EngineType engineType;

    private int seats;              // Número de asientos
}
