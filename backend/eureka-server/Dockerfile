FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY eureka-server   ./eureka-server
COPY gateway-service ./gateway-service
COPY auth-service    ./auth-service
COPY car-service     ./car-service
COPY booking-service ./booking-service

RUN mvn clean package -pl eureka-server -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-server/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]