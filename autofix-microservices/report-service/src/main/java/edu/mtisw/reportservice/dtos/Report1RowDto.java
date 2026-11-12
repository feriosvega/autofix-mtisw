package edu.mtisw.reportservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor
public class Report1RowDto {
    private Long repairTypeId; private String repairName;
    private Map<String, Long> countsByVehicleType; private Map<String, Long> amountByVehicleType;
    private long totalCount; private long totalAmount;
}
