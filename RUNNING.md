Running and Testing the Auth Microservice
Run AuthServiceApplication.java. It should start on port 8083.

Access H2 Console: http://localhost:8083/h2-console (JDBC URL: jdbc:h2:mem:authdb). 
You should see users and roles tables, and user_roles linking them. 
The roles (ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN) should be pre-populated.

Test Endpoints (e.g., with Postman):

Register a User (POST request):

URL: http://localhost:8083/api/auth/signup
Method: POST
Headers: Content-Type: application/json
Body:
JSON

{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "role": ["user"]
}
Expected Response: 200 OK with {"message": "User registered successfully!"}.
Login User (POST request):

URL: http://localhost:8083/api/auth/signin
Method: POST
Headers: Content-Type: application/json
Body:
JSON

{
    "username": "testuser",
    "password": "password123"
}
Expected Response: 200 OK with a JwtResponse containing the JWT token. Copy this token.