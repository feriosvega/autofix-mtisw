package edu.mtisw.reportservice.dtos;
import lombok.Data;
import java.util.List;
@Data
public class RepairReportDataDto {
    private Long repairId; private Long vehicleId; private String plate; private String vehicleType;
    private List<Detail> details;
    @Data public static class Detail { private Long repairType; private String repairName; private String repairDate; private String repairTime; private long amount; }
}
