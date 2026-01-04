# GOAT Watches Backend

This is the backend service for the GOAT Watches application, built with Spring Boot.

## Prerequisites

*   Java 21
*   Maven

## Environment Variables

To run the application successfully, you need to set the following environment variables:

*   `RAPID_API_KEY`: Your API key for the RapidAPI Watch Data API.
*   `DB_PASSWORD`: The password for the database (if applicable, e.g., for a production PostgreSQL instance). For local development with H2, this might not be strictly required depending on your configuration, but it's good practice to be aware of it.

## Running the Application

You can run the application using Maven:

```bash
./mvnw spring-boot:run
```

Or by building the JAR and running it:

```bash
./mvnw clean package
java -jar target/goat-watches-backend-1.0.0.jar
```

## Running Tests

To run the tests, execute:

```bash
./mvnw test
```

Note: Ensure you have a `src/test/resources/application-test.properties` file with your `api.rapid.key` set for integration tests that hit the RapidAPI.
