-- Create database
CREATE DATABASE IF NOT EXISTS secure_banking;
USE secure_banking;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    INDEX idx_email (email),
    INDEX idx_phone (phone_number)
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'USD',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    daily_transfer_limit DECIMAL(19, 2) DEFAULT 10000.00,
    monthly_transfer_limit DECIMAL(19, 2) DEFAULT 50000.00,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_user_id (user_id)
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(500) NOT NULL,
    failure_reason VARCHAR(500),
    fee DECIMAL(19, 2) DEFAULT 0.00,
    flagged_for_review BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_source_account (source_account_id),
    INDEX idx_destination_account (destination_account_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
);

-- Create database user
CREATE USER IF NOT EXISTS 'bank_user'@'localhost' IDENTIFIED BY 'SecurePassword123!';
GRANT ALL PRIVILEGES ON secure_banking.* TO 'bank_user'@'localhost';
FLUSH PRIVILEGES;

-- Insert sample admin user (password: Admin123!)
INSERT INTO users (email, password, first_name, last_name, phone_number, address, role, active, email_verified)
VALUES ('admin@securebank.com', '$2a$10$xQPxqEeOYt7X8KFqPQs0E.dXQ3KqLX8FvYYwN9qTY7KQYcFqY7Y0u', 
        'Admin', 'User', '+1234567890', '123 Admin Street, City, State', 'ADMIN', TRUE, TRUE)
ON DUPLICATE KEY UPDATE email = email;



/* COMMENTS:```

4. **Save the file**

---

## 🎊 **BACKEND 100% COMPLETE!**

---

## 🎯 **What's Next?**

We need to create the **FRONTEND** (React files)!

---

## 📝 **Frontend Files We Need:**
```
frontend/
├── package.json
├── vite.config.js
├── index.html
└── src/
    ├── App.jsx
    ├── main.jsx
    ├── index.css
    └── components/
        ├── Login.jsx
        ├── Register.jsx
        ├── Dashboard.jsx
        ├── Navbar.jsx
        └── Transfer.jsx COMMENT    */