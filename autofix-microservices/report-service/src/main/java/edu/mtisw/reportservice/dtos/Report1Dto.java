package edu.mtisw.reportservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor
public class Report1Dto {
    private int year; private int month; private List<String> vehicleTypes;
    private List<Report1RowDto> rows; private Map<String, Long> totalCountsByVehicleType;
    private Map<String, Long> totalAmountByVehicleType; private long totalCount; private long totalAmount;
}
