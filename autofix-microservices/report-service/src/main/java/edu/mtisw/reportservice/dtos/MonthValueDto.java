package edu.mtisw.reportservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @NoArgsConstructor @AllArgsConstructor
public class MonthValueDto {
    private int year;
    private int month;
    private String label;
    private long quantity;
    private long amount;
    private long deltaQuantity;
    private long deltaAmount;
    private Double quantityVariationPercent;
    private Double amountVariationPercent;
}
