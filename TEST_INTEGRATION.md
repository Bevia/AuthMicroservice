# Terminal 1: Start Auth service
cd /Users/vincentbevia/IdeaProjects/AuthMicroservice && ./mvnw spring-boot:run

# Terminal 2: Start Order service  
cd /Users/vincentbevia/IdeaProjects/OrderMicroservice && ./mvnw spring-boot:run

# Terminal 3: Start Payment service
cd /Users/vincentbevia/IdeaProjects/PaymentMicroservice && ./mvnw spring-boot:run

# Terminal 4: Test the flow
# 1. Register user (if not already done)
curl -X POST http://localhost:8083/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","role":["user"]}'

# 2. Get JWT token
TOKEN=$(curl -s -X POST http://localhost:8083/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"

# 3. Access Order service (port 8080)
curl http://localhost:8080/api/orders -H "Authorization: Bearer $TOKEN"

# 4. Access Payment service (port 8081)
curl http://localhost:8081/api/payments -H "Authorization: Bearer $TOKEN"

# 5. Without token - should get 403 Forbidden
curl http://localhost:8080/api/orders


I created the workspace file E-Commerce-Microservices.code-workspace.

To open it:

File → Open Workspace from File...
Navigate to IdeaProjects
Select E-Commerce-Microservices.code-workspace
Or run in terminal:

code /Users/vincentbevia/IdeaProjects/E-Commerce-Microservices.code-workspace

## Remember to start service:
# Terminal for Order service
cd /Users/vincentbevia/IdeaProjects/OrderMicroservice && ./mvnw spring-boot:run

# Terminal for Payment service
cd /Users/vincentbevia/IdeaProjects/PaymentMicroservice && ./mvnw spring-boot:run