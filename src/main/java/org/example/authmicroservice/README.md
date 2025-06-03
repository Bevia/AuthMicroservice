## Setting up project

Core Responsibilities of an Authentication Microservice:

User Management: Registering new users, managing user details (passwords, roles).
Authentication: Verifying user credentials (username/password) during login.
Token Issuance (JWT): Generating secure JWTs upon successful authentication.
Token Validation (Implicit/Stateless): While the Auth Service issues tokens, other services (Order, Payment) will primarily validate these tokens themselves using a shared secret/public key. The Auth Service can provide a validation endpoint for more complex scenarios (e.g., token revocation), but for stateless JWTs, direct validation is common.
Authorization (Role/Permission Data): Providing user roles/permissions, which are often embedded in the JWT.
Key Technologies/Concepts for JWT-based Auth Microservice:
Spring Security: The powerful security framework for Spring applications.
JWT (JSON Web Tokens): A compact, URL-safe means of representing claims to be transferred between two parties. It's self-contained and digitally signed.
JJWT Library: A popular Java library for creating and parsing JWTs.
BCrypt: A strong hashing algorithm for securely storing user passwords.
Tokenization (for user data): While JWTs are tokens, "tokenization" in the payment sense often refers to replacing sensitive data (like credit card numbers) with a non-sensitive equivalent. For authentication, JWTs are the tokens, but you'll avoid putting sensitive PII directly into the JWT payload.
Step-by-Step: Building the Auth Microservice
Step 1: Create a New Spring Boot Project (in IntelliJ IDEA)
Open IntelliJ IDEA.
Go to File > New > Project...
Select "Maven" (or Gradle).
Choose "Spring Initializr" and click Next.
Step 2: Configure Project Metadata
Group: com.example.auth
Artifact: auth-service
Name: auth-service
Description: Authentication Microservice
Package Name: com.example.auth
Java Version: Match your other services.
Packaging: Jar
Language: Java
Click Next.
Step 3: Select Dependencies (CRITICAL for Auth Service)
Spring Web: For REST APIs.
Spring Data JPA: For database interaction (users, roles).
H2 Database: For development (or PostgreSQL/MySQL driver).
Spring Security: This is the core authentication and authorization framework.
Lombok (Optional): For boilerplate reduction.
Spring Boot DevTools (Optional): For faster restarts.
JJWT API, Impl, Jackson: You'll need these for JWT handling. You'll add these manually to pom.xml after creation, as Spring Initializr doesn't offer them directly.
Click Create.

Step 4: Add JJWT Dependencies to pom.xml
After the project is created, open your pom.xml and add these dependencies within the <dependencies> section:

XML

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version> </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version> <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version> <scope>runtime</scope>
        </dependency>

Important: After adding these, remember to Reload Maven Changes in IntelliJ IDEA.

## Usage of JWTs
How Other Microservices (Order, Payment) Will Use This
The Auth Service issues the token. The real power of JWTs in microservices is that other services can validate them statelessly. They don't need to call the Auth Service for every incoming request.

1. Add JWT Validation to Other Microservices (e.g., Order Service, Payment Service)
   Dependencies: Add spring-boot-starter-security, jjwt-api, jjwt-impl, jjwt-jackson to their pom.xml files.
   JWT Secret: Configure app.jwt.secret in their application.properties to be the exact same secret as in your Auth Service.
   JWT Filter: Implement a OncePerRequestFilter that:
   Extracts the JWT from the Authorization: Bearer <token> header.
   Uses a copy of the JwtUtils class (or just the validation logic) to validate the token using the shared secret.
   If valid, extracts the username and roles from the token.
   Creates a Spring Security Authentication object and sets it in the SecurityContextHolder. This allows Spring Security's @PreAuthorize and method-level security to work.
   Security Configuration: Configure WebSecurityConfig in each service to:
   Disable CSRF and sessions.
   Add your custom JWT filter.
   Secure endpoints (e.g., /api/orders/** requires authentication).
2. Getting User Information (if needed)
   If a service needs more user information than what's in the JWT (e.g., user's address, full name), it would make a synchronous API call to the Auth Service (e.g., GET /api/users/{userId}). This endpoint in the Auth Service would itself be protected.

This is a comprehensive setup for your Authentication Microservice. It will allow you to secure your other microservices and manage user access effectively.

