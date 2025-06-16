# 🛒 Full-Stack Ecommerce Platform

A full-featured ecommerce platform built as a personal project to demonstrate scalable architecture, event-driven design, caching strategies, and observability. The system supports real-time operations, admin dashboards, and production-grade DevOps practices.

---

## ✨ Key Features

### 🔐 Authentication & Access Control
- JWT-based signup/login with refresh token support
- Role-Based Access Control (RBAC) — Admin vs Customer
- Email verification via token-based activation

### 🛍 Ecommerce Features
- Product catalog with ratings & reviews (1 review per user/product)
- Add to cart, update, and clear cart operations
- Order placement (COD & Razorpay online payment)
- Verified purchase badge for reviews
- Address management

### 🛠 System Architecture
- Kafka-based message queues for all **write operations** (create/update/delete)
- Redis for **caching** product, cart, and review data with intelligent eviction
- PostgreSQL for production + H2 for isolated testing
- Swagger/OpenAPI documentation for all REST APIs
- Rate limiting for public endpoints

### 📈 Monitoring & Admin Analytics
- Prometheus metrics collection
- Grafana dashboards
- Admin-only analytics:
  - Top-selling products
  - Revenue over time
  - Orders grouped by status
- Admin interfaces to manage products and orders

### ✅ Testing
- 70%+ line coverage with **JUnit + Mockito**
- H2 in-memory DB for integration tests
- Controller, service, and repository layers thoroughly tested

---

## 🧰 Tech Stack

| Layer        | Tech / Tools                           |
|--------------|-----------------------------------------|
| Frontend     | React, TypeScript, Tailwind CSS         |
| Backend      | Java, Spring Boot, Spring Security      |
| Database     | PostgreSQL (prod), H2 (test)            |
| Caching      | Redis                                   |
| Messaging    | Apache Kafka                            |
| Payments     | Razorpay (test mode)                    |
| Auth         | JWT, Email verification, RBAC           |
| Monitoring   | Prometheus, Grafana                     |
| Docs         | Swagger / OpenAPI                       |
| Testing      | JUnit, Mockito                          |
| Deployment   | Docker, Docker Compose                  |

---

## 🚀 Getting Started

### 🧱 Backend Setup (Spring Boot)

```bash
git clone https://github.com/your-username/ecommerce-platform.git
cd ecommerce-platform/backend

# Run with Maven
./mvnw spring-boot:run
```

```bash
 Frontend Setup (React + TypeScript)

cd ../frontend
npm install
npm run dev
```

```bash
Dockerized Setup

docker compose up --build
```


