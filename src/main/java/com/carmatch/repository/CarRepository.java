package com.carmatch.repository;

import com.carmatch.entity.Car;
import com.carmatch.enums.CarStatus;
import com.carmatch.enums.CarType;
import com.carmatch.enums.FuelType;
import com.carmatch.enums.TransmissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByStatusAndIsDeletedFalse(CarStatus status);
    List<Car> findByIsDeletedFalse();

    @Query("SELECT c FROM Car c WHERE c.status = 'APPROVED' AND c.isDeleted = false " +
            "AND (:keyword IS NULL OR LOWER(c.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.model) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:fuelType IS NULL OR c.fuelType = :fuelType) " +
            "AND (:carType IS NULL OR c.carType = :carType) " +
            "AND (:transmission IS NULL OR c.transmission = :transmission) " +
            "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
            "AND (:seats IS NULL OR c.seatingCapacity >= :seats)")
            Page<Car> searchCars(
            @Param("keyword") String keyword,
            @Param("fuelType") FuelType fuelType,
            @Param("carType") CarType carType,
            @Param("transmission") TransmissionType transmission,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("seats") Integer seats,
            Pageable pageable);
}