FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service ./gateway-service
COPY auth-service ./auth-service
COPY car-service ./car-service
COPY booking-service ./booking-service
RUN mvn clean package -pl booking-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN mkdir /data
WORKDIR /app
COPY --from=build /app/booking-service/target/booking-service-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]