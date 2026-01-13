# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create directory for uploaded photos
RUN mkdir -p /app/photos

# Copy jar file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 1999

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
