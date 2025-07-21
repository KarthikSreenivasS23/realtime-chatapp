# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the Maven wrapper and pom.xml to download dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy the rest of the application source code
COPY src ./src

# Build the application, skipping tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create media directories
RUN mkdir -p /app/root/picture /app/root/video /app/root/profile

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/chatapp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
