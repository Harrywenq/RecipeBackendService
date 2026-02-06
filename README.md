# Recipe Service

Backend service built with **Spring Boot** providing RESTful APIs with authentication and database integration.

## Tech Stack
- Java 17
- Spring Boot 3.4.x
- Spring Web
- Spring Data JPA
- Spring Security
- Maven
- Lombok
- Docker

---

## Project Structure
```
src/main/java/com/huytpq/SecurityEx
├── base
│ ├── annotations # Custom annotations
│ ├── config # Global configurations
│ ├── data # Common data/constants
│ ├── dto # Base DTOs
│ ├── exception # Global exception handling
│ ├── mapper # Object mappers
│ ├── security # Security core logic
│ └── util # Utility classes
│
├── recipe
│ ├── controller # REST controllers
│ ├── dto # Request/Response DTOs
│ ├── entity # JPA entities
│ ├── mapper # Entity-DTO mappers
│ ├── repo # JPA repositories
│ └── service # Business logic
```

---

## Requirements
- JDK 17+
- Maven 3.8+
- Docker (optional)

---

## Configuration
Application configuration is located at:

src/main/resources/application.properties


Typical configuration includes:
- Server port
- Database connection
- JPA settings
- Security configuration

Example:
```properties
server.port=8090

spring.datasource.url=jdbc:mysql://localhost:3306/example_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
Build & Run (Local)
```
Build project
mvn clean package -DskipTests
```
Run application
```
mvn spring-boot:run
```
Or run the jar directly:
```
java -jar target/*.jar
```
Docker
- Build Docker image
```
docker build -t recipe-service .
```
Run container
```
docker run -p 8090:8090 recipe-service
```
## Dockerfile
- The project uses a multi-stage build to optimize image size:

- Stage 1: Build jar using Maven

- Stage 2: Run application with OpenJDK 17

## API
- Base URL: http://localhost:8090

- APIs are exposed via REST controllers.

- Authentication & authorization are handled using Spring Security.

- (API documentation can be added later.)
