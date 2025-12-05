# AuthMicroservice - Copilot Instructions

## Architecture Overview

This is a **JWT-based Authentication Microservice** built with Spring Boot 3.5, designed for microservices architectures. It handles user registration, authentication, and JWT token issuance for stateless communication.

### Core Flow
1. **Signup** (`POST /api/auth/signup`) → Validates uniqueness → BCrypt hashes password → Assigns roles → Persists `User`
2. **Signin** (`POST /api/auth/signin`) → Authenticates via `AuthenticationManager` → Issues JWT with username as subject

### Key Components
| Package | Purpose |
|---------|---------|
| `controller/` | REST endpoints - `AuthController` handles `/api/auth/*` |
| `jwt/` | `JwtUtils` - token generation/validation using JJWT 0.12.5 |
| `model/` | JPA entities: `User`, `Role`, `ERole` enum |
| `security/` | `WebSecurityConfig` - stateless security, BCrypt, public auth endpoints |
| `service/` | Spring Security's `UserDetailsService` implementation |
| `payload/` | Request/Response DTOs (`LoginRequest`, `SignupRequest`, `JwtResponse`) |

## Development Workflow

### Run the Service
```bash
./mvnw spring-boot:run
```
- Runs on **port 8083**
- H2 Console: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:authdb`)

### Role Initialization
Roles (`ROLE_USER`, `ROLE_MODERATOR`, `ROLE_ADMIN`) are auto-seeded via `CommandLineRunner` in `AuthMicroservice.java` on startup.

### Test Endpoints
```bash
# Signup
curl -X POST http://localhost:8083/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","role":["user"]}'

# Signin
curl -X POST http://localhost:8083/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## Project Conventions

### DTOs & Payloads
- Use `@Data` from Lombok for all DTOs in `payload/request/` and `payload/response/`
- Request DTOs: `*Request.java`, Response DTOs: `*Response.java`

### Entity Patterns
- JPA entities use Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- User-Role relationship: `@ManyToMany` with `FetchType.EAGER` and `user_roles` join table
- Roles stored as `@Enumerated(EnumType.STRING)` via `ERole` enum

### Security Configuration
- CSRF disabled (stateless JWT auth)
- Session policy: `STATELESS`
- Public endpoints: `/api/auth/**`, `/h2-console/**`
- Password encoding: `BCryptPasswordEncoder`

### JWT Configuration
- Secret and expiration configured in `application.properties`:
  - `app.jwt.secret` - Base64-encoded HMAC key
  - `app.jwt.expiration-ms` - Token TTL (default: 3600000ms = 1hr)
- JWT subject contains **username only** (avoid PII in tokens)

## Adding New Features

### New Role
1. Add to `ERole` enum
2. Role auto-seeds if using existing `CommandLineRunner` pattern

### New Protected Endpoint
1. Add to controller with `@PreAuthorize` annotation (enabled via `@EnableMethodSecurity`)
2. Endpoints outside `/api/auth/**` require authentication by default

### Repository Queries
Follow Spring Data JPA conventions - see `UserRepository` for patterns:
- `findByUsername()`, `existsByUsername()`, `existsByEmail()`

## JWT Validation for Other Microservices

Other services (Order, Payment, etc.) should validate JWTs **stateless** without calling back to this Auth service.

### Required Setup in Consumer Services
1. **Share the same secret** (`app.jwt.secret`) via environment variable or config server
2. **Add JJWT dependencies** to the consumer service's `pom.xml`:
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

3. **Create a JWT filter** that extracts and validates the token from `Authorization: Bearer <token>` header
4. **Copy `JwtUtils.java`** validation logic - key methods:
   ```java
   // Validate signature and expiration
   Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token);
   
   // Extract username from token subject
   String username = Jwts.parser().setSigningKey(key()).build()
       .parseClaimsJws(token).getBody().getSubject();
   ```

### Token Structure
- **Subject**: Username (use for user lookup/identification)
- **Issued At**: Token creation timestamp
- **Expiration**: 1 hour from issuance (configurable via `app.jwt.expiration-ms`)
- **Algorithm**: HS256 (HMAC-SHA256)

### Security Note
The JWT does **not** contain roles in the payload. Consumer services must either:
- Call Auth service's user endpoint to fetch roles (adds latency)
- Extend `JwtUtils.generateJwtToken()` to include roles as claims (recommended)
