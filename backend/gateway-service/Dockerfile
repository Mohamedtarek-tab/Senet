FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service ./gateway-service
COPY auth-service ./auth-service
COPY car-service ./car-service
COPY booking-service ./booking-service
RUN mvn clean package -pl gateway-service -am -DskipTests

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /app/gateway-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
