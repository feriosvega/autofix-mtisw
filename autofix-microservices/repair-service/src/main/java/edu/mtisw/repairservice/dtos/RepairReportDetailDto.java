package edu.mtisw.repairservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairReportDetailDto {
    private Long repairType;
    private String repairName;
    private LocalDate repairDate;
    private LocalTime repairTime;
    private long amount;
}
