package com.carmatch.dto.response;

import com.carmatch.enums.*;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public TransmissionType getTransmission() { return transmission; }
    public void setTransmission(TransmissionType transmission) { this.transmission = transmission; }

    public CarType getCarType() { return carType; }
    public void setCarType(CarType carType) { this.carType = carType; }

    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }

    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }
}