package edu.mtisw.repairservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "repair_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairDetailEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long repairType;
    private String repairName;
    private LocalDate repairDate;
    private LocalTime repairTime;
    private long price;
}
