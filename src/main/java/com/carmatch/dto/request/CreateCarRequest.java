package com.carmatch.dto.request;

import com.carmatch.enums.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCarRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be after 1900")
    @Max(value = 2030, message = "Year must be before 2030")
    private Integer year;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotNull(message = "Transmission is required")
    private TransmissionType transmission;

    @NotNull(message = "Car type is required")
    private CarType carType;

    @NotNull(message = "Seating capacity is required")
    @Min(value = 1, message = "Seating must be at least 1")
    @Max(value = 10, message = "Seating cannot exceed 10")
    private Integer seatingCapacity;

    @NotNull(message = "Usage type is required")
    private UsageType usageType;

    private Double mileage;

    private String description;
}