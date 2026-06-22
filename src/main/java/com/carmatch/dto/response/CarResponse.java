package com.carmatch.dto.response;

import com.carmatch.enums.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CarResponse {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private Double price;
    private FuelType fuelType;
    private TransmissionType transmission;
    private CarType carType;
    private Integer seatingCapacity;
    private UsageType usageType;
    private Double mileage;
    private String description;
    private CarStatus status;
    private LocalDateTime createdAt;
}