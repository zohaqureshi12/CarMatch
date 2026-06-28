package com.carmatch.controller;

import com.carmatch.dto.request.CreateCarRequest;
import com.carmatch.dto.request.UpdateCarRequest;
import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.CarResponse;
import com.carmatch.dto.response.CarSummaryResponse;
import com.carmatch.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.carmatch.enums.CarType;
import com.carmatch.enums.FuelType;
import com.carmatch.enums.TransmissionType;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cars", description = "Car management endpoints")
public class CarController {

    @Autowired
    private CarService carService;

    //  User Endpoints

    @GetMapping("/api/cars")
    @Operation(summary = "Search and filter approved cars")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CarSummaryResponse>>> searchCars(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) CarType carType,
            @RequestParam(required = false) TransmissionType transmission,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer seats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<CarSummaryResponse> cars = carService.searchCars(
                keyword, fuelType, carType, transmission,
                minPrice, maxPrice, seats,
                page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.success("Cars retrieved successfully", cars));
    }

    @GetMapping("/api/cars/{id}")
    @Operation(summary = "Get single car detail")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> getCarById(@PathVariable Long id) {
        CarResponse car = carService.getCarById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Car retrieved successfully", car));
    }

    //  Admin Endpoints

    @PostMapping("/api/admin/cars")
    @Operation(summary = "Create a new car")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> createCar(
            @Valid @RequestBody CreateCarRequest request) {
        CarResponse car = carService.createCar(request);
        return ResponseEntity.ok(
                ApiResponse.success("Car created successfully", car));
    }

    @PutMapping("/api/admin/cars/{id}")
    @Operation(summary = "Update a car")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequest request) {
        CarResponse car = carService.updateCar(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Car updated successfully", car));
    }

    @DeleteMapping("/api/admin/cars/{id}")
    @Operation(summary = "Soft delete a car")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok(
                ApiResponse.success("Car deleted successfully", null));
    }

    @PatchMapping("/api/admin/cars/{id}/approve")
    @Operation(summary = "Approve a pending car")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> approveCar(@PathVariable Long id) {
        CarResponse car = carService.approveCar(id);
        return ResponseEntity.ok(
                ApiResponse.success("Car approved successfully", car));
    }

    @PatchMapping("/api/admin/cars/{id}/reject")
    @Operation(summary = "Reject a pending car")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> rejectCar(@PathVariable Long id) {
        CarResponse car = carService.rejectCar(id);
        return ResponseEntity.ok(
                ApiResponse.success("Car rejected successfully", car));
    }

    @GetMapping("/api/admin/cars")
    @Operation(summary = "Get all cars - admin view")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CarSummaryResponse>>> getAllCarsAdmin() {
        List<CarSummaryResponse> cars = carService.getAllCarsAdmin();
        return ResponseEntity.ok(
                ApiResponse.success("Cars retrieved successfully", cars));
    }

    @GetMapping("/api/admin/cars/pending")
    @Operation(summary = "Get all pending cars")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CarSummaryResponse>>> getPendingCars() {
        List<CarSummaryResponse> cars = carService.getPendingCars();
        return ResponseEntity.ok(
                ApiResponse.success("Pending cars retrieved successfully", cars));
    }
}