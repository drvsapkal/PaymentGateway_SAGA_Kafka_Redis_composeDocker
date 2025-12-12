# ================================
# Stage 1: Build saga-shared & payment-service
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# 1️⃣ Copy saga-shared library and install it
COPY saga-shared /app/saga-shared
RUN mvn -f /app/saga-shared/pom.xml clean install -DskipTests

# 2️⃣ Copy payment-service source
COPY payment-service /app/payment-service
RUN mvn -f /app/payment-service/pom.xml clean package -DskipTests

# ================================
# Stage 2: Create lightweight runtime image
# ================================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/payment-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
