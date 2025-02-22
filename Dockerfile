FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
COPY --from=build /target/Hotel-Booking-Api-0.0.1-SNAPSHOT.jar Hotel-Booking-Api-0.0.1-SNAPSHOT.jar
EXPOSE 9090
ENTRYPOINT [ "java", "-jar", "Hotel-Booking-Api-0.0.1-SNAPSHOT.jar" ]