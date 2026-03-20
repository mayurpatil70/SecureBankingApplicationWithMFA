# 🏦 Secure Banking Application

A full-stack banking application built with Spring Boot and React.

## 🚀 Features

- ✅ User Authentication (JWT)
- ✅ Multi-Factor Authentication (MFA/2FA)
- ✅ Account Management (Checking & Savings)
- ✅ Money Transfers
- ✅ Transaction History with Search & Filters
- ✅ Account Statements (PDF Export)
- ✅ Profile Management
- ✅ Password Reset
- ✅ Account Lockout Protection

## 🛠️ Tech Stack

**Backend:**

- Java 17
- Spring Boot 3.2.0
- Spring Security
- MySQL 8.0
- JWT Authentication
- iText PDF

**Frontend:**

- React 18
- Vite
- Axios
- React Router
- CSS3

## 📦 Installation

### Prerequisites

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### Backend Setup

1. Navigate to backend folder:

```bash
cd backend
```

2. Update `application.properties` with your MySQL credentials

3. Run the application:

```bash
mvn spring-boot:run
```

Backend runs on: http://localhost:8081

### Frontend Setup

1. Navigate to frontend folder:

```bash
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start development server:

```bash
npm run dev
```

Frontend runs on: http://localhost:3000

## 🗄️ Database Setup

```sql
CREATE DATABASE secure_banking;
CREATE USER 'bank_user'@'localhost' IDENTIFIED BY 'SecurePassword123!';
GRANT ALL PRIVILEGES ON secure_banking.* TO 'bank_user'@'localhost';
FLUSH PRIVILEGES;
```

Run the schema file: `backend/src/main/resources/schema.sql`

## 📸 Screenshots

[Add screenshots here]

## 🔐 Security Features

- BCrypt password hashing
- JWT token-based authentication
- TOTP Multi-Factor Authentication
- Account lockout after failed attempts
- CORS protection
- SQL injection prevention

## 👤 Author

Your Name

## 📄 License

This project is for educational purposes
