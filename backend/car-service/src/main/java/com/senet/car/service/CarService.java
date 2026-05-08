package com.senet.car.service;

import com.senet.car.model.Car;
import com.senet.car.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public Car createCar(Car car) {
        if (car.getStatus() == null) {
            car.setStatus("available");
        }
        return carRepository.save(car);
    }

    public Car updateCar(Long id, Car updatedCar) {
        return carRepository.findById(id).map(car -> {
            if (updatedCar.getBrand() != null) car.setBrand(updatedCar.getBrand());
            if (updatedCar.getModel() != null) car.setModel(updatedCar.getModel());
            if (updatedCar.getYear() != null) car.setYear(updatedCar.getYear());
            if (updatedCar.getPricePerDay() != null) car.setPricePerDay(updatedCar.getPricePerDay());
            if (updatedCar.getCategory() != null) car.setCategory(updatedCar.getCategory());
            if (updatedCar.getEngine() != null) car.setEngine(updatedCar.getEngine());
            if (updatedCar.getTransmission() != null) car.setTransmission(updatedCar.getTransmission());
            if (updatedCar.getImageUrl() != null) car.setImageUrl(updatedCar.getImageUrl());
            if (updatedCar.getDescription() != null) car.setDescription(updatedCar.getDescription());
            if (updatedCar.getStatus() != null) car.setStatus(updatedCar.getStatus());
            return carRepository.save(car);
        }).orElseThrow(() -> new RuntimeException("Car not found"));
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }
}
