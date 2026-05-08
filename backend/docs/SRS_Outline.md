# Software Requirements Specification (SRS) - SENET Luxury Car Rental Backend

## 1. Introduction
### 1.1 Purpose
This document specifies the software requirements for the SENET Luxury Car Rental backend system. It outlines the microservices architecture, API definitions, and database structure.
### 1.2 Scope
The system handles Identity & Authentication, Fleet Management, Booking System, and simulated Payment Processing via a microservices architecture.

## 2. Overall Description
### 2.1 Product Perspective
The backend is a Spring Boot application acting as a standalone backend serving a vanilla HTML/JS frontend via a Spring Cloud Gateway. It persists data to local SQLite databases mapped to Docker volumes.
### 2.2 User Classes and Characteristics
- **Client**: Can register, login, view cars, book a car, and manage their own bookings.
- **Admin**: Can perform full CRUD operations on the car fleet and manage all system bookings.

## 3. Specific Requirements
### 3.1 Functional Requirements
- **FR1 (Auth)**: The system shall allow users to register and login, returning a JWT.
- **FR2 (Fleet)**: The system shall allow Admin users to add, update, and delete cars.
- **FR3 (Fleet)**: The system shall allow all users to view available cars.
- **FR4 (Booking)**: The system shall allow logged-in users to reserve a car.
- **FR5 (Payment)**: The system shall simulate payment processing and update booking status to confirmed.
### 3.2 Non-Functional Requirements
- **NFR1 (Performance)**: API requests shall be logged with execution time using Aspect-Oriented Programming (AOP).
- **NFR2 (Security)**: The Gateway shall validate JWTs and restrict unauthenticated access.
- **NFR3 (Deployment)**: The system must be deployable via Docker Compose.
