FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service ./gateway-service
COPY auth-service ./auth-service
COPY car-service ./car-service
COPY booking-service ./booking-service
RUN mvn clean package -pl auth-service -am -DskipTests

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
RUN mkdir /data
COPY --from=build /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
