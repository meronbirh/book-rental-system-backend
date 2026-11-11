# Book Rental Management System – Backend

A RESTful backend application for managing book rentals, library inventory, members, rental payments, and related library operations.

Built with **Java, Spring Boot, and PostgreSQL**, this project provides backend services and APIs for a book rental management system.

## Technologies Used

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Git and GitHub**
- **Postman** – API testing

## Main Features

### Book and Inventory Management
- Book registration and management
- Book category management
- Book copy tracking
- Duplicate ISBN validation
- Book copy inspection
- Book disposal management

### User and Member Management
- User registration and login APIs
- Username and email uniqueness validation
- Member registration and profile management
- User roles (`OWNER`, `STAFF`, and `MEMBER`)
- Account approval and rejection workflow
- Account activation and deactivation
- Password reset and password change functionality
- Automatic member profile and wallet creation during registration

### Rental Management
- Book rental processing
- Book return management
- Rental fee calculation
- Overdue fine management

### Wallet and Transactions
- Member wallet management
- Wallet top-ups
- Rental payment processing through the internal wallet
- Wallet transaction history

### Additional Features
- Donation management
- Donation item management
- Fine waiver requests
- Notification management
- Wallet configuration

## Project Architecture

The backend follows a layered architecture to separate responsibilities and improve maintainability.

```text
src/main/java/com/example/bookrentalsystem/
│
├── controller/    REST API endpoints
├── service/       Business logic
├── repository/    Database operations
├── entity/        JPA entities
├── dto/           Data Transfer Objects
│
└── BookRentalSystemApplication.java
```

## Database

The application uses PostgreSQL for persistent data storage.

Default local database configuration:

- Database name: `book_rental_system`
- Database username: `postgres`
- Database URL: `jdbc:postgresql://localhost:5432/book_rental_system`

The database must be created before running the application.

## Getting Started

### Prerequisites

Install the following:

- Java Development Kit (JDK) 21
- PostgreSQL
- Git

### 1. Clone the repository

```bash
git clone https://github.com/meronbirh/book-rental-system-backend.git
```

### 2. Navigate to the project directory

```bash
cd book-rental-system-backend
```

### 3. Create the PostgreSQL database

```sql
CREATE DATABASE book_rental_system;
```

### 4. Configure database credentials

Create the following file:

`src/main/resources/application-local.properties`

Add your local database password:

```properties
spring.datasource.password=YOUR_DATABASE_PASSWORD
```

Replace `YOUR_DATABASE_PASSWORD` with your own PostgreSQL password.

This local configuration file is excluded from Git to prevent database credentials from being committed.

### 5. Run the application

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

### 6. Build the project

On Windows:

```cmd
mvnw.cmd clean package
```

On Linux or macOS:

```bash
./mvnw clean package
```

## Testing

Run the automated tests using the Maven wrapper:

```cmd
mvnw.cmd test
```

The automated test suite is currently limited and will be expanded as development continues.

API endpoints are also tested manually using Postman.

## Current Development Status

The project is under active development. Future improvements include expanded automated testing, enhanced authentication security, and API documentation.

## Project Repository

GitHub: https://github.com/meronbirh/book-rental-system-backend

---

This project is part of my practical learning and portfolio development in Java backend development, REST API design, database management, and software engineering.
