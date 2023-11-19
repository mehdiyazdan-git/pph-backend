# Use the official OpenJDK image as the base image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the executable JAR file from your build target directory to the container
COPY target/*.jar app.jar

# Expose the port that your Spring Boot app listens on
EXPOSE 8080

# Define the command to run your Spring Boot app when the container starts
CMD ["java", "-jar", "app.jar"]
