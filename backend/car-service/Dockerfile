FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service ./gateway-service
COPY auth-service ./auth-service
COPY car-service ./car-service
COPY booking-service ./booking-service
RUN mvn clean package -pl car-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN mkdir /data
WORKDIR /app
COPY --from=build /app/car-service/target/car-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]