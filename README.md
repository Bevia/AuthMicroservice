# Authentication Microservice

This document provides a comprehensive guide to setting up and understanding the core functionalities of the Authentication Microservice. Designed for a microservices architecture, this service efficiently handles user management, authentication, and authorization, primarily utilizing JSON Web Tokens (JWTs) for secure and stateless communication.

---

## Core Responsibilities

The Authentication Microservice is central to managing user access and security, with the following key responsibilities:

* **User Management**: Facilitating the registration of new users and the ongoing management of user details, including passwords and roles.
* **Authentication**: Verifying user credentials (e.g., username and password) during the login process.
* **Token Issuance (JWT)**: Generating secure JSON Web Tokens upon successful user authentication.
* **Token Validation (Implicit/Stateless)**: While this service issues tokens, other microservices (such as Order and Payment services) are primarily responsible for validating these tokens themselves using a shared secret or public key. The Authentication Service can offer a validation endpoint for more complex scenarios, like token revocation, but direct, stateless validation is common for JWTs.
* **Authorization (Role/Permission Data)**: Providing user roles and permissions, which are typically embedded within the issued JWTs.

---

## Key Technologies and Concepts

The development and operation of this JWT-based Authentication Microservice rely on the following critical technologies and concepts:

* **Spring Security**: A robust and highly customizable security framework for Spring-based applications, providing comprehensive authentication and authorization services.
* **JWT (JSON Web Tokens)**: A compact, URL-safe means of representing claims securely between two parties. JWTs are self-contained and digitally signed, making them ideal for stateless authentication in distributed systems.
* **JJWT Library**: A popular and easy-to-use Java library specifically designed for creating, parsing, and validating JWTs.
* **BCrypt**: A strong and adaptive cryptographic hashing function used for securely storing user passwords, offering excellent resistance against brute-force attacks.
* **Tokenization (for user data)**: While JWTs themselves are tokens for authentication, it's crucial to distinguish this from "tokenization" in the payment context (replacing sensitive data with non-sensitive equivalents). For authentication, JWTs are the primary tokens, and **sensitive Personally Identifiable Information (PII) should be avoided in the JWT payload.**

---

## Step-by-Step Guide: Building the Authentication Microservice

Follow these detailed steps to set up your Authentication Microservice project.

### Step 1: Create a New Spring Boot Project

1.  Open **IntelliJ IDEA**.
2.  Navigate to `File` > `New` > `Project...`.
3.  Select `Maven` (or Gradle) and choose `Spring Initializr`.
4.  Click **Next**.

### Step 2: Configure Project Metadata

Provide the following metadata for your project:

* **Group**: `com.example.auth`
* **Artifact**: `auth-service`
* **Name**: `auth-service`
* **Description**: `Authentication Microservice`
* **Package Name**: `com.example.auth`
* **Java Version**: Ensure this matches the Java version used across your other services.
* **Packaging**: `Jar`
* **Language**: `Java`

Click **Next**.

### Step 3: Select Dependencies

Select the following essential dependencies. Note that specific JJWT dependencies will be added manually in the next step as they are not directly available via Spring Initializr.

* **Spring Web**: Enables the creation of RESTful APIs.
* **Spring Data JPA**: Facilitates interaction with relational databases for managing users and roles.
* **H2 Database**: A lightweight in-memory database suitable for development and testing. (You can replace this with drivers for PostgreSQL, MySQL, etc., for production.)
* **Spring Security**: The foundational framework for implementing authentication and authorization.
* **Lombok** (Optional): Reduces boilerplate code by automatically generating getters, setters, constructors, etc.
* **Spring Boot DevTools** (Optional): Provides development-time features such as automatic restarts and LiveReload.

Click **Create**.

### Step 4: Add JJWT Dependencies to `pom.xml`

After your project has been created, open the `pom.xml` file and insert the following dependencies within the `<dependencies>` section:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

**Important**: After adding these dependencies, it is crucial to **Reload Maven Changes** in IntelliJ IDEA to ensure they are properly recognized and integrated into your project.

---

## Usage of JWTs in Other Microservices

The true power of JWTs in a microservices architecture lies in their ability to enable stateless validation by other services. Once the Authentication Service issues a token, subsequent services (e.g., Order Service, Payment Service) can validate it independently without needing to communicate with the Authentication Service for every incoming request.

### 1. Add JWT Validation to Other Microservices

To enable other microservices (e.g., Order Service, Payment Service) to validate JWTs, follow these steps:

* **Dependencies**: Add `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, and `jjwt-jackson` to the `pom.xml` files of these services.
* **JWT Secret**: Configure the `app.jwt.secret` property in their `application.properties` file. This secret **must be identical** to the secret used by your Authentication Service for signing tokens.
* **JWT Filter**: Implement a custom `OncePerRequestFilter` that performs the following actions:
   * Extracts the JWT from the `Authorization: Bearer <token>` header of incoming requests.
   * Validates the extracted token using the shared secret (you can reuse the validation logic from a `JwtUtils` class or similar utility).
   * If the token is valid, it extracts the username and roles embedded within the token's payload.
   * It then creates a Spring Security `Authentication` object and sets it in the `SecurityContextHolder`, which allows Spring Security's `@PreAuthorize` and method-level security annotations to function correctly.
* **Security Configuration**: Configure the `WebSecurityConfig` in each service to:
   * Disable CSRF (Cross-Site Request Forgery) protection and session management, as JWTs are stateless.
   * Add your custom JWT filter to the Spring Security filter chain.
   * Define and secure endpoints (e.g., `/api/orders/**` might require authenticated access).

### 2. Getting User Information (if needed)

If a downstream microservice requires more detailed user information than what is available in the JWT payload (e.g., a user's full address or contact details), it should make a synchronous API call to the Authentication Service. For instance, a `GET /api/users/{userId}` endpoint could be exposed by the Authentication Service. This endpoint itself **must be protected** by JWT validation to ensure only authorized services can access sensitive user data.

---
