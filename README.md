# Health Check Monitoring Project #healthz

This project includes features for checking the database connection’s health.

## Introduction for the project

The Database Health Check is a Spring Boot application designed to perform database connectivity. It includes one main controllers: `HealthController` for verifying database connectivity.

## Prerequisites for project

Before building and deploying the application locally, ensure you have the following prerequisites:

- Java Development Kit (JDK) 17
- Maven

## Health Check Controller

The `HealthController` It manages health check requests to confirm the database connection status. It offers endpoint for verifying the database connection, handling unsupported HTTP methods, and managing unknown URLs


### Endpoints for app

Here's a rephrased version:

- `/healthz`: Checks the database connection and manages specific conditions.
- `/healthz` (POST, PUT, PATCH, DELETE, HEAD, OPTIONS, TRACE): Manages method-not-allowed responses for certain HTTP methods.

## Configuration

The project configuration file (`application.properties`) to manage database connectivity

## Commands to call Health Endpoint
### Success
curl -vvvv http://localhost:8080/healthz

### Failue
curl -vvvv http://localhost:8080/healthz

### 405 Method Not Allowed
curl -vvvv -XPUT http://localhost:8080/healthz

## Usage

To run the project locally, follow below steps:

1. Clone the repository.
2. Configure the `application.properties` file with your database settings.
3. Build the project using Maven: mvn clean install.
4. Run the application.


