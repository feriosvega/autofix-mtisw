package edu.mtisw.reportservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class Report2RowDto { private Long repairTypeId; private String repairName; private List<MonthValueDto> months; }
