package edu.mtisw.reportservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class Report2Dto { private int year; private int month; private List<MonthValueDto> months; private List<Report2RowDto> rows; }
