<div align="center">
  <h1>🎟️ SyncEvent - Event Management System</h1>
  <p><strong>A comprehensive, high-performance platform for orchestrating events, managing attendees, and processing ticketing revenue.</strong></p>
  
  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
  [![Razorpay](https://img.shields.io/badge/Payment-Razorpay-blueviolet.svg)](https://razorpay.com/)
  [![JWT](https://img.shields.io/badge/Security-JWT-black.svg)](https://jwt.io/)
</div>

---

## 📖 Overview

**SyncEvent** is a robust, role-based Event Management System (EMS) designed to streamline the entire lifecycle of event hosting and attendance. Whether you're an attendee looking for the next big conference, an organizer managing ticket sales and venue capacities, or an admin overseeing platform integrity, SyncEvent provides a unified, secure, and intuitive API architecture.

Built with scale and reliability in mind, the platform features a strict state-machine for event lifecycles, real-time ticket inventory management, and mathematically secure payment integrations.

---

## ✨ Key Features by Role

### 👤 Attendees
*   **Discover & Explore:** Search for published events using advanced filters (category, location, price range, and dates).
*   **Secure Bookings:** Add tickets to your cart, apply promotional codes, and checkout seamlessly via Razorpay integration.
*   **Manage Profiles & Tickets:** View chronological booking histories, manage guest lists, and retrieve secure QR-ready ticket codes.
*   **Post-Event Engagement:** Leave qualitative feedback and ratings for events you've attended.
*   **Self-Service Cancellations:** Cancel eligible bookings within deadlines and track automated refunds.

### 🏢 Organizers
*   **Complete Event Lifecycle:** Draft events, submit them for admin approval, and publish them to the world.
*   **Venue & Capacity Management:** Bind events to physical venues, ensuring ticket tiers never exceed mathematical venue constraints.
*   **Ticketing & Promotions:** Create multiple ticket tiers (e.g., VIP, Early Bird) and orchestrate targeted promotional campaigns with usage limits.
*   **Real-time Analytics:** Access deep-dive reports detailing revenue, ticket sales velocity, and participant demographics.
*   **Gate Management:** Dynamically check-in participants at the door and monitor real-time attendance.

### 🛡️ Administrators
*   **Global Moderation:** Control user access, suspend accounts, and approve or reject new organizer applications.
*   **Platform Oversight:** Manage global lists of event categories and physical venues.
*   **System Analytics:** Benchmark overall platform health, comparing organizer performance and aggregating global revenue trends.
*   **Communication:** Execute targeted broadcast messages or system-wide announcements to all user cohorts.

---

## 🛠️ Technical Architecture

SyncEvent is powered by a modern, enterprise-grade Java stack, designed for maintainability, security, and high cohesion.

*   **Core Framework:** **Java 21** with **Spring Boot 4.0.4**
*   **Data Persistence:** **MySQL** via AWS RDS, managed by **Spring Data JPA** & **Hibernate**.
*   **Security Layer:** **Spring Security** combined with stateless **JSON Web Tokens (JWT)** for robust Role-Based Access Control (RBAC). Implements OTP-based verification.
*   **Payment Gateway:** **Razorpay Java SDK (1.4.3)** featuring cryptographic HMAC-SHA256 signature verification for zero-trust transactional integrity.
*   **Mail Engine:** **Spring Boot Starter Mail** for automated notifications and OTP delivery.
*   **Password Hashing:** **jBCrypt** for secure credential storage.

---

## ⚙️ System Modules Deep Dive

*   **State Machine (Event Lifecycle):** Events transition strictly through `DRAFT` ➔ `PENDING APPROVAL` ➔ `APPROVED` ➔ `PUBLISHED`. Modifications are locked down once an event goes live to protect active buyers.
*   **Booking & Inventory Engine:** Features an atomic dry-run cart calculator. When a booking is initiated, inventory is mathematically reserved to prevent race conditions before payment callback verification.
*   **Promotional Engine:** A real-time validation engine that checks code validity against time boundaries, aggregate usage limits, and dynamically calculates percentage discounts capped at absolute maximums.
*   **Domain-Driven Structure:** Code is segmented into tightly scoped feature packages (`auth`, `booking`, `payment`, `event`, etc.), ensuring boundaries are respected.

---

## 🚀 Getting Started

Follow these instructions to set up the project locally for development and testing.

### Prerequisites
*   [Java Development Kit (JDK) 21](https://jdk.java.net/21/)
*   [Maven](https://maven.apache.org/) (Or use the provided `./mvnw` wrapper)
*   [MySQL Server](https://dev.mysql.com/downloads/mysql/)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd EventManagementsystem
```

### 2. Configure Environment Variables
The application relies on several environment variables for security and integration. You can set these in your OS environment or create a `.env` file if your IDE supports it. Ensure the following are configured matching `application.properties`:

| Variable Name | Description | Example |
| :--- | :--- | :--- |
| `DB_PASSWORD` | Password for the MySQL database. | `supersecretdbpass` |
| `JWT_SECRET` | A strong, 256-bit secure key for signing JWTs. | `your_very_long_secure_jwt_secret_key` |
| `MAIL_USERNAME` | SMTP Email Address (e.g., Gmail). | `noreply@syncevent.com` |
| `MAIL_PASSWORD` | App Password for the SMTP email. | `abcd efgh ijkl mnop` |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID. | `rzp_test_XXXXXXXX` |
| `RAZORPAY_KEY_SECRET`| Razorpay API Secret. | `secret_XXXXXXXX` |

*Note: The database URL is currently pointed to an AWS RDS instance (`jdbc:mysql://ems-db.cy3wg6s8qzru.us-east-1.rds.amazonaws.com:3306/ems_db`). Update `spring.datasource.url` in `application.properties` if you wish to run a local database.*

### 3. Build and Run
Use the Maven wrapper to clean, compile, and start the Spring Boot application:

**Linux/macOS:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The server will start on port `8082`. The base API path is `/api`.

---

## 📁 Project Structure

The codebase is organized by feature domain to promote separation of concerns:

```text
src/main/java/com/project/ems/
├── auth/           # JWT Filters, OTP logic, Login/Register controllers
├── booking/        # Cart calculations, registration entities, booking lifecycle
├── category/       # Event categories management
├── common/         # Shared utilities, global exception handlers
├── config/         # Spring Security, CORS, Swagger, and Gateway configurations
├── event/          # Core event logic, lifecycle management, and filtering
├── notification/   # Broadcasts and email dispatch services
├── organizer/      # Organizer profile verifications and dashboards
├── participant/    # Individual ticket holders and gate check-in logic
├── payment/        # Razorpay webhook verification and transaction logs
├── refund/         # Automated refund processing and retries
├── report/         # Aggregated analytics and revenue calculations
├── ticket/         # Ticket tiers and real-time inventory tracking
└── venue/          # Physical location and capacity definitions
```

---

## 📚 API Documentation

For a comprehensive breakdown of all available endpoints, required payloads, business logic rules, and role-based access restrictions, please refer to the detailed [API Documentation](api_documentation.md) file included in the repository.

---

> **Note to Developers:** When contributing, ensure adherence to the existing REST conventions and update the `api_documentation.md` and `api_endpoint_logic.md` appropriately when introducing new endpoints.
