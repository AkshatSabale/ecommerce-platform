# 🛒 Full-Stack Ecommerce Platform

This is a full-featured ecommerce platform built as a personal project to demonstrate production-grade architecture and hands-on experience with scalable backend systems, real-time event processing, caching, and admin-level analytics. Developed using **Java (Spring Boot)** for the backend and **React + TypeScript** for the frontend.

---

## ✨ Key Features

### 🔐 Authentication & Access Control
- JWT-based login/signup
- Role-Based Access Control (RBAC) for admin and customer segregation
- Email verification and token-based activation flow

### 🛍 Ecommerce Functionality
- Product listing, details, and reviews
- Cart and order management
- Razorpay integration (test mode) for online payments
- Cash on Delivery (COD) option
- Verified purchase tick on user reviews

### 🛠 System Architecture
- Kafka message queues for write operations (create, update, delete)
- Redis caching with intelligent eviction
- PostgreSQL for production, H2 for test suite
- REST APIs documented using Swagger/OpenAPI
- Rate limiting for public endpoints

### 📈 Monitoring & Analytics
- Prometheus for metrics, Grafana dashboards
- Admin-only analytics:
    - Top-selling products
    - Revenue over time
    - Orders by status
- Admin dashboards for product & order control

### ✅ Testing
- JUnit + Mockito for unit/integration testing
- 70%+ line coverage
- H2 in-memory DB for isolated test environments

---

## 🧰 Tech Stack

| Layer       | Tools / Frameworks                           |
|-------------|-----------------------------------------------|
| **Frontend**| React, TypeScript, Tailwind CSS               |
| **Backend** | Java, Spring Boot, Spring Security, Kafka     |
| **Database**| PostgreSQL (prod), H2 (testing)               |
| **Caching** | Redis                                         |
| **Queueing**| Apache Kafka                                  |
| **Payments**| Razorpay (test mode)                          |
| **Testing** | JUnit, Mockito                                |
| **Monitoring**| Prometheus, Grafana                        |
| **Docs**    | Swagger / OpenAPI                             |
| **Auth**    | JWT, Role-based access                        |

---

## 🚀 Getting Started

### 🧱 Backend Setup (Spring Boot)

```bash
git clone https://github.com/your-username/ecommerce-platform.git
cd ecommerce-platform/backend

# Run with Maven
./mvnw spring-boot:run
