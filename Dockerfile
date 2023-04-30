FROM amazoncorretto:11
WORKDIR /app
COPY build/libs/shutters-0.1.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# Set the default command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
