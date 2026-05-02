# Use a lightweight JRE image for production
FROM eclipse-temurin:25-jre

# Set the working directory inside the container
WORKDIR /app

# Expose the port your Spring Boot app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]