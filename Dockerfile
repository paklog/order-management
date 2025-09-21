# Multi-stage build for optimization
# Build stage
FROM eclipse-temurin:21-jdk AS builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/order-management-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user
RUN addgroup --system spring && \
    adduser --system spring --ingroup spring

# Change ownership of the JAR file
RUN chown spring:spring app.jar

# Switch to the non-root user
USER spring

# Expose the ports the app uses
EXPOSE 8080 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
