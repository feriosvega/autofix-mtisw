package edu.mtisw.repairservice.entities;

import lombok.Data;

@Data
public class VehicleInfo {
    private Long id;
    private String plate;
    private String brand;
    private String model;
    private String vehicleType;
    private int manufactureYear;
    private String engineType;
    private int seats;
}
