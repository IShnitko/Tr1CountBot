# Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
# Corrected path: copy from the build context root
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the entire project and build it
# Corrected path: copy from the build context root
COPY . .
RUN mvn clean package -DskipTests

# Use a lightweight JRE image to run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
