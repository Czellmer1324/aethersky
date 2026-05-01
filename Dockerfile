# Use a lightweight JRE image for production
FROM eclipse-temurin:25-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the pre-built JAR from your local build folder
# Adjust the source path to match where your JAR is generated (e.g., build/libs/ or target/)
COPY output/mastercontrol/libs/mastercontrol-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]