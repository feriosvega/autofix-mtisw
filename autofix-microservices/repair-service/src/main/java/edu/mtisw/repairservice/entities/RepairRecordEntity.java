package edu.mtisw.repairservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long vehicleId;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDate exitDate;
    private LocalTime exitTime;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private int mileage;
    private boolean applyBonus;     // La empresa decide a qué vehículo aplicarle un bono de su marca
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "repair_record_id")
    private List<RepairDetailEntity> details = new ArrayList<>();
    private long repairsAmount;
    private long mileageSurcharge;
    private long ageSurcharge;
    private long lateSurcharge;
    private long repairsCountDiscount;
    private long dayDiscount;
    private long bonusDiscount;
    private long subtotal;
    private long iva;
    private long totalAmount;
}
