package edu.mtisw.repairservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairReportDataDto {
    private Long repairId;
    private Long vehicleId;
    private String plate;
    private String vehicleType;
    private List<RepairReportDetailDto> details;
}
