package edu.mtisw.autofixbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Bonos mensuales que envía la concesionaria TopCar (Toyota, Ford, Hyundai, Honda)
@Entity
@Table(name = "bonuses", uniqueConstraints = @UniqueConstraint(columnNames = {"brand", "bonusYear", "bonusMonth"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String brand;
    private int bonusYear;
    private int bonusMonth;
    private int quantity;           // Bonos disponibles (se descuenta al usarlos)
    private long amount;            // Monto de cada bono

    @Version
    private Long version;           // Bloqueo optimista: 3 réplicas del backend podrían usar el mismo bono
}
