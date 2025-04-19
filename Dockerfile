# Use an official OpenJDK 17 runtime as the base image
FROM eclipse-temurin:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and POM files first (to take advantage of Docker caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execute permissions to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies (this step caches dependencies and speeds up future builds)
RUN ./mvnw dependency:go-offline

# Copy the rest of the application files
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080 for the Spring Boot application
EXPOSE 8080

# Find the JAR file in the target directory
CMD ["sh", "-c", "java -jar target/$(ls target | grep '.jar' | head -n 1)"]