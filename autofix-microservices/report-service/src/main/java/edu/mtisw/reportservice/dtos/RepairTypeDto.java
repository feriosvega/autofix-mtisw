package edu.mtisw.reportservice.dtos;
import lombok.Data;
@Data
public class RepairTypeDto {
    private Long id; private String name; private long priceGasoline; private long priceDiesel;
    private long priceHybrid; private long priceElectric;
}
