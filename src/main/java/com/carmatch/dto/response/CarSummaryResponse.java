package com.carmatch.dto.response;

import com.carmatch.enums.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarSummaryResponse {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private Double price;
    private FuelType fuelType;
    private TransmissionType transmission;
    private CarType carType;
    private Integer seatingCapacity;
    private CarStatus status;
}