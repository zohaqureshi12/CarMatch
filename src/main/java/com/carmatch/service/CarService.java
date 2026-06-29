package com.carmatch.service;

import com.carmatch.dto.request.CreateCarRequest;
import com.carmatch.dto.request.UpdateCarRequest;
import com.carmatch.dto.response.CarResponse;
import com.carmatch.dto.response.CarSummaryResponse;
import com.carmatch.entity.Car;
import com.carmatch.enums.CarStatus;
import com.carmatch.enums.CarType;
import com.carmatch.enums.FuelType;
import com.carmatch.enums.TransmissionType;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    // Map Entity to Full Response
    public CarResponse mapToCarResponse(Car car) {
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setYear(car.getYear());
        response.setPrice(car.getPrice());
        response.setFuelType(car.getFuelType());
        response.setTransmission(car.getTransmission());
        response.setCarType(car.getCarType());
        response.setSeatingCapacity(car.getSeatingCapacity());
        response.setUsageType(car.getUsageType());
        response.setMileage(car.getMileage());
        response.setDescription(car.getDescription());
        response.setImageUrl(car.getImageUrl());
        response.setStatus(car.getStatus());
        response.setCreatedAt(car.getCreatedAt());
        return response;
    }

    // Map Entity to Summary Response
    public CarSummaryResponse mapToCarSummaryResponse(Car car) {
        CarSummaryResponse response = new CarSummaryResponse();
        response.setId(car.getId());
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setYear(car.getYear());
        response.setPrice(car.getPrice());
        response.setFuelType(car.getFuelType());
        response.setTransmission(car.getTransmission());
        response.setCarType(car.getCarType());
        response.setSeatingCapacity(car.getSeatingCapacity());
        response.setStatus(car.getStatus());
        response.setImageUrl(car.getImageUrl());
        response.setIsDeleted(car.getIsDeleted());
        return response;
    }

    //  Create Car (Admin only)
    public CarResponse createCar(CreateCarRequest request) {
        Car car = new Car();
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setPrice(request.getPrice());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setCarType(request.getCarType());
        car.setSeatingCapacity(request.getSeatingCapacity());
        car.setUsageType(request.getUsageType());
        car.setMileage(request.getMileage());
        car.setDescription(request.getDescription());
        car.setImageUrl(request.getImageUrl());
        car.setStatus(CarStatus.PENDING);
        carRepository.save(car);
        return mapToCarResponse(car);
    }

    //  Update Car (Admin only)
    public CarResponse updateCar(Long id, UpdateCarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + id));

        if (car.getIsDeleted()) {
            throw new ResourceNotFoundException(
                    "Car not found with id: " + id);
        }

        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setPrice(request.getPrice());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setCarType(request.getCarType());
        car.setSeatingCapacity(request.getSeatingCapacity());
        car.setUsageType(request.getUsageType());
        car.setMileage(request.getMileage());
        car.setDescription(request.getDescription());
        car.setImageUrl(request.getImageUrl());
        carRepository.save(car);
        return mapToCarResponse(car);
    }

    //  Soft Delete Car (Admin only)
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + id));
        car.setIsDeleted(true);
        carRepository.save(car);
    }

    //  Approve Car (Admin only)
    public CarResponse approveCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + id));
        car.setStatus(CarStatus.APPROVED);
        carRepository.save(car);
        return mapToCarResponse(car);
    }

    //  Reject Car (Admin only)
    public CarResponse rejectCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + id));
        car.setStatus(CarStatus.REJECTED);
        carRepository.save(car);
        return mapToCarResponse(car);
    }

    //  Get Single Car (User)
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + id));

        if (car.getIsDeleted()) {
            throw new ResourceNotFoundException(
                    "Car not found with id: " + id);
        }

        return mapToCarResponse(car);
    }

    // Get All Cars (Admin)
    public List<CarSummaryResponse> getAllCarsAdmin() {
        return carRepository.findAll()
                .stream()
                .map(this::mapToCarSummaryResponse)
                .collect(Collectors.toList());
    }

    // Get Pending Cars (Admin)
    public List<CarSummaryResponse> getPendingCars() {
        return carRepository.findByStatusAndIsDeletedFalse(CarStatus.PENDING)
                .stream()
                .map(this::mapToCarSummaryResponse)
                .collect(Collectors.toList());
    }

    // Get Approved Cars (User)
    public List<CarSummaryResponse> getApprovedCars() {
        return carRepository.findByStatusAndIsDeletedFalse(CarStatus.APPROVED)
                .stream()
                .map(this::mapToCarSummaryResponse)
                .collect(Collectors.toList());
    }

    // Search + Filter + Sort + Paginate (User)
    public Page<CarSummaryResponse> searchCars(
            String keyword,
            FuelType fuelType,
            CarType carType,
            TransmissionType transmission,
            Double minPrice,
            Double maxPrice,
            Integer seats,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return carRepository.searchCars(
                        keyword, fuelType, carType,
                        transmission, minPrice, maxPrice,
                        seats, pageable)
                .map(this::mapToCarSummaryResponse);
    }
}