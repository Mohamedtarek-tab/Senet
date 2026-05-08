package com.senet.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private String id; // Format: BK-001, etc.

    private String car;
    private Long carId;
    private String client;
    private String userId; // Link to user
    private String pickup;
    private String ret; // Return date
    private String amount; // EGP 7,200
    private String status; // pending, confirmed, completed, cancelled

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCar() { return car; }
    public void setCar(String car) { this.car = car; }
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }
    public String getRet() { return ret; }
    public void setRet(String ret) { this.ret = ret; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
