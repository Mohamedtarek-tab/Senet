package com.senet.car.controller;

import com.senet.car.model.Car;
import com.senet.car.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carService.getCarById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCar(@Valid @RequestBody Car car) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCar(car));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCar(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        return carService.getCarById(id).map(car -> {
            if (body.containsKey("status"))       car.setStatus((String) body.get("status"));
            if (body.containsKey("brand"))        car.setBrand((String) body.get("brand"));
            if (body.containsKey("model"))        car.setModel((String) body.get("model"));
            if (body.containsKey("pricePerDay"))  car.setPricePerDay(((Number) body.get("pricePerDay")).doubleValue());
            if (body.containsKey("year"))         car.setYear(((Number) body.get("year")).intValue());
            if (body.containsKey("category"))     car.setCategory((String) body.get("category"));
            if (body.containsKey("engine"))       car.setEngine((String) body.get("engine"));
            if (body.containsKey("transmission")) car.setTransmission((String) body.get("transmission"));
            if (body.containsKey("imageUrl"))     car.setImageUrl((String) body.get("imageUrl"));
            if (body.containsKey("description"))  car.setDescription((String) body.get("description"));
            return ResponseEntity.ok(carService.updateCar(id, car));  // ← updateCar not save
        }).orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/{id}/status")
public ResponseEntity<?> updateCarStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
    return carService.getCarById(id).map(car -> {
        car.setStatus(body.get("status"));
        return ResponseEntity.ok(carService.updateCar(id, car));
    }).orElse(ResponseEntity.notFound().build());
}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}