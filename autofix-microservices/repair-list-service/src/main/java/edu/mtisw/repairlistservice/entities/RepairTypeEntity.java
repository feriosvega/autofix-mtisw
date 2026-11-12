package edu.mtisw.repairlistservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// HU2: tipo de reparación que ofrece el taller y su precio según el tipo de motor.
// Precio 0 = la reparación no aplica a ese motor (ej. sistema de escape en un eléctrico).
@Entity
@Table(name = "repair_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;                // Nro. de la reparación (1..11 en el enunciado; se pueden añadir más)

    @Column(unique = true, nullable = false)
    private String name;

    private long priceGasoline;
    private long priceDiesel;
    private long priceHybrid;
    private long priceElectric;

    public long priceFor(EngineType engineType) {
        return switch (engineType) {
            case GASOLINA -> priceGasoline;
            case DIESEL -> priceDiesel;
            case HIBRIDO -> priceHybrid;
            case ELECTRICO -> priceElectric;
        };
    }
}
