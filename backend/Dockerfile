# Use OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Give execution permission to mvnw
RUN chmod +x mvnw

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose the port Render assigns
ENV PORT 10000
EXPOSE 10000

RUN ls -ltr target/
# rename the jar file
RUN cp target/goat-watches-backend-1.0.0.jar app.jar

# Run the jar
ENTRYPOINT ["sh", "-c", "exec java -Duser.timezone=Asia/Kolkata -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev} -jar app.jar"]
