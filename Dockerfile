# Use Java 21 base image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Give execute permission to Maven wrapper
RUN chmod +x mvnw

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests

# Expose application port
EXPOSE 8080

# Start Spring Boot app
CMD ["sh", "-c", "java -jar target/*.jar"]
