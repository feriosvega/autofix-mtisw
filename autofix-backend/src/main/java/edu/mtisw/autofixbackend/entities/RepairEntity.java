package edu.mtisw.autofixbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// HU2: Ingreso de un vehículo al taller (historial de reparaciones) + resultado del cálculo HU3
@Entity
@Table(name = "repairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    // Relación N:1 con el vehículo (FK repairs.vehicle_id -> vehicles.id). Se escribe vía vehicleId.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private VehicleEntity vehicle;

    // Datos del historial exigidos por el enunciado
    private LocalDate entryDate;    // Fecha ingreso al taller
    private LocalTime entryTime;    // Hora ingreso al taller
    private LocalDate exitDate;     // Fecha salida de la reparación (vehículo listo)
    private LocalTime exitTime;     // Hora salida de la reparación
    private LocalDate pickupDate;   // Fecha en que el cliente retiró el vehículo
    private LocalTime pickupTime;   // Hora en que el cliente retiró el vehículo

    private int mileage;            // Kilometraje al ingresar (recargo por kilometraje)
    private boolean applyBonus;     // La empresa decide si aplica un bono de la marca

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "repair_id")
    private List<RepairDetailEntity> details = new ArrayList<>();

    // Desglose del cálculo (HU3). Se guardan para el historial y para el reporte R1.
    private long repairsAmount;         // Suma(Reparaciones)
    private long mileageSurcharge;      // Recargo por kilometraje
    private long ageSurcharge;          // Recargo por antigüedad
    private long lateSurcharge;         // Recargo por retraso en la recogida
    private long repairsCountDiscount;  // Descuento por nro. de reparaciones (últimos 12 meses)
    private long dayDiscount;           // Descuento por día de atención
    private long bonusDiscount;         // Descuento por bono
    private long subtotal;              // Suma + Recargos - Descuentos
    private long iva;                   // IVA 19%
    private long totalAmount;           // Monto total de la reparación
}
