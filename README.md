# Nook API (Booking Backend System)

A high-load RESTful API for a housing booking service, with object storage and caching integration.

## Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.5 (Spring Web, Spring Data JPA, Spring Security, Validation, Actuator, Mail)
* **Database:** PostgreSQL
* **Database Migration:** Flyway
* **Caching:** Redis
* **Object Storage:** MinIO (S3-compatible)
* **Authentication:** JWT (JJWT)
* **Payments:** Stripe
* **API Documentation:** Swagger / OpenAPI (SpringDoc)
* **Mapping:** MapStruct
* **Build Tool:** Maven
* **Testing:** JUnit 5, Spring Boot Test, Spring Security Test, Testcontainers
* **Utilities:** Lombok

## Architecture

The application follows a layered architecture with clear separation of concerns:

* **Controller Layer** – Handles HTTP requests and API endpoints.
* **Service Layer** – Contains business logic and domain rules.
* **Repository Layer** – Provides data access through Spring Data JPA.
* **DTO & Mapper Layer** – Uses MapStruct for entity-to-DTO transformations.
* **Security Layer** – JWT-based authentication and role-based authorization.
* **Infrastructure Layer** – Integrates Redis, MinIO, Stripe, Flyway, and asynchronous event processing.

The project is designed around modular services, making the codebase easier to maintain, test, and extend.

## Key Features

### Authentication & Authorization

* JWT Access & Refresh Token authentication.
* Role-based access control (USER, HOST, ADMIN).
* Token blacklisting for secure logout.
* Method-level security with Spring Security.

### Booking System

* Property booking workflow.
* Availability validation for overlapping reservations.
* Booking cancellation support.
* Automatic expiration handling for unpaid bookings.

### Payment Processing

* Stripe Checkout integration.
* Stripe Webhook processing.
* Payment status tracking.
* Refund support.

### Listing Management

* Create, update, and delete property listings.
* Image upload support using MinIO object storage.
* Dynamic filtering with JPA Specifications.
* Pagination and sorting support.

### Reviews & Ratings

* User review system.
* Automatic rating aggregation.
* Average rating and review count calculation.

### Performance Optimizations

* Redis caching for filtered listing searches.
* Custom cache key generation.
* EntityGraph and optimized fetching strategies.
* Batch loading and subselect fetching for collections.

### Reliability & Security

* Global exception handling.
* Request validation using Bean Validation.
* Redis-based rate limiting.
* Optimistic locking with JPA @Version.

### Background Processing

* Asynchronous email notifications.
* Event-driven architecture using Spring Events.
* Scheduled jobs for automatic cleanup of expired bookings and payments.

### API Documentation

* OpenAPI 3 / Swagger UI integration.
* JWT authorization support inside Swagger.
* Fully documented REST endpoints.

### Testing

* Integration testing with Testcontainers.
* Spring Security testing support.
* Automated coverage reports with JaCoCo.


## Project launch

### Requirements
* Java 21+
* Docker and Docker compose

### Step 1:
```bash

git clone [https://github.com/thed1as/Nook](https://github.com/thed1as/Nook)
cd Nook
```

### Step 2:

Create a .env file in the root directory and populate it with:
```
# Database
DB_HOST=example
DB_USERNAME=example_admin
DB_PASSWORD=examplePassword!

# MinIO
MINIO_HOST=example_minio
MINIO_ACCESS_KEY=example_minio_admin
MINIO_SECRET_KEY=example_minio_secret_key!
MINIO_BUCKET=example_bucket

# Security & Stripe
ACCESS_SECURITY_KEY=...
REFRESH_SECURITY_KEY=...
STRIPE_SECRET_KEY=...
STRIPE_PUBLISHABLE_KEY=...
STRIPE_WEBHOOK_SECRET=...

MAIL_HOST=smtp.mail.com
MAIL_USERNAME=testtest@gmail.com
```

### Step 3
Infrastructure launch
```bash

docker-compose up -d
```
This command will bring up PostgreSQL, Redis and Minio

### Step 4
```bash

./mvnw spring-boot:run
```

### Stripe Webhooks
To test you have to start listening for Stripe events:

```bash

stripe listen --forward-to localhost:8080/api/v1/stripe-notifications
```

To simulate a successful card payment:

```bash

stripe payment_intents confirm pi_... --payment-method=pm_card_visa
```

This command triggers Stripe webhook events that will be processed by the application.

## Main API Endpoints


| Method | Endpoint                             | Description                             |
| ------ | ------------------------------------ | --------------------------------------- |
| POST   | `/auth/register`                     | Register a new user                     |
| POST   | `/auth/login`                        | Authenticate user and obtain JWT tokens |
| POST   | `/auth/refresh`                      | Refresh access token                    |
| POST   | `/auth/logout`                       | Logout and invalidate tokens            |
| GET    | `/api/v1/listings`                   | Retrieve all listings                   |
| GET    | `/api/v1/listings/search`            | Search listings using filters           |
| GET    | `/api/v1/listing/{id}`               | Retrieve listing details                |
| POST   | `/api/v1/listing`                    | Create a new listing                    |
| PUT    | `/api/v1/listing/{id}`               | Update an existing listing              |
| DELETE | `/api/v1/listing/{id}`               | Delete a listing                        |
| POST   | `/api/v1/listing/{id}/images`        | Upload listing images                   |
| POST   | `/api/v1/booking`                    | Create a booking                        |
| GET    | `/api/v1/booking/{id}`               | Retrieve booking details                |
| GET    | `/api/v1/bookings/my`                | Retrieve current user's bookings        |
| DELETE | `/api/v1/bookings/{id}`              | Cancel a booking                        |
| GET    | `/api/v1/payments/my`                | Retrieve payment history                |
| GET    | `/api/v1/listing/{id}/reviews`       | Retrieve listing reviews                |
| POST   | `/api/v1/listing/{id}/reviews`       | Create a review                         |
| PUT    | `/api/v1/listing/reviews/{reviewId}` | Update a review                         |
| DELETE | `/api/v1/listing/reviews/{reviewId}` | Delete a review                         |
| GET    | `/api/v1/user/{id}`                  | Retrieve user profile                   |
| POST   | `/api/v1/user/me/role`               | Upgrade account to HOST                 |

## API Documentation

Interactive API documentation is available via Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

After starting the application, open the URL above to explore and test all available endpoints.
