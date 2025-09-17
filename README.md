# 🛍️ Vintage Vogue - E-commerce Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)](https://www.thymeleaf.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> A comprehensive e-commerce platform specializing in vintage clothing and accessories, built with modern Java technologies and featuring a complete marketplace ecosystem.

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

## 🎯 Overview

Vintage Vogue is a full-featured e-commerce platform designed for the vintage fashion market. The application provides a complete marketplace experience with user management, product catalog, shopping cart, order processing, real-time messaging, and comprehensive admin capabilities.

### Key Highlights

- **🛒 Complete E-commerce Solution**: Product management, shopping cart, order processing, and payment integration
- **👥 Multi-role User System**: Customers, sellers, and administrators with role-based access control
- **💬 Real-time Communication**: WebSocket-based messaging system for buyer-seller communication
- **📱 Responsive Design**: Mobile-first approach with Bootstrap and modern CSS
- **🔒 Security First**: Spring Security with JWT authentication and email verification
- **📊 Admin Dashboard**: Comprehensive management tools for products, orders, users, and support tickets

## ✨ Features

### 🛍️ **E-commerce Core**
- **Product Catalog**: Browse, search, and filter vintage clothing and accessories
- **Shopping Cart**: Add/remove items, quantity management, and cart persistence
- **Order Management**: Complete order lifecycle from placement to delivery tracking
- **Payment Integration**: Multiple payment methods including bank transfers
- **Inventory Management**: Real-time stock tracking and availability updates

### 👤 **User Management**
- **User Registration & Authentication**: Secure account creation with email verification
- **Profile Management**: User profiles with address and contact information
- **Password Recovery**: Secure password reset via email tokens
- **Role-based Access**: Customer, seller, and admin roles with appropriate permissions
- **Account Security**: Account locking and verification requirements

### 🏪 **Marketplace Features**
- **Multi-vendor Support**: Users can sell their own vintage items
- **Product Listings**: Detailed product pages with multiple images and descriptions
- **Category Management**: Organized product categorization system
- **Search & Filtering**: Advanced search capabilities with category and price filters
- **Favorites System**: Save products for later viewing

### 💬 **Communication & Support**
- **Real-time Messaging**: WebSocket-based chat between buyers and sellers
- **Support Ticket System**: Customer support with ticket tracking and responses
- **Notification System**: Real-time notifications for orders, messages, and updates
- **Comment System**: Product reviews and user feedback

### 🔧 **Admin Features**
- **Dashboard**: Comprehensive admin panel with analytics and management tools
- **User Management**: User account administration and role management
- **Product Management**: Add, edit, and manage product listings
- **Order Management**: Process and track customer orders
- **Category Management**: Organize and manage product categories
- **Support Management**: Handle customer support tickets and responses
- **Advertisement Management**: Manage promotional content and banners

### 🎨 **User Experience**
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **Modern UI/UX**: Clean, intuitive interface with Bootstrap components
- **Interactive Elements**: SweetAlert2 for enhanced user interactions
- **Image Management**: Multiple product images with upload and optimization
- **Maintenance Mode**: System maintenance capabilities with user notifications

## 🛠️ Technology Stack

### **Backend**
- **Java 17**: Modern Java features and performance optimizations
- **Spring Boot 3.3.1**: Rapid application development framework
- **Spring Security**: Authentication, authorization, and security features
- **Spring Data JPA**: Data persistence and repository pattern
- **Spring WebSocket**: Real-time communication capabilities
- **Spring Mail**: Email services for notifications and verification
- **Hibernate**: Object-relational mapping and database abstraction
- **MySQL 8.0**: Relational database for data persistence
- **JWT**: JSON Web Tokens for secure authentication
- **Maven**: Dependency management and build automation

### **Frontend**
- **Thymeleaf 3.1**: Server-side template engine for dynamic content
- **Bootstrap 5**: Responsive CSS framework for modern UI
- **JavaScript (ES6+)**: Client-side interactivity and AJAX operations
- **jQuery**: DOM manipulation and event handling
- **SweetAlert2**: Beautiful and responsive alert dialogs
- **WebSocket Client**: Real-time messaging functionality

### **Development Tools**
- **Spring Boot DevTools**: Hot reload and development utilities
- **Spring Boot Actuator**: Application monitoring and health checks
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for testing
- **Thumbnailator**: Image processing and optimization

## 🏗️ Architecture

### **MVC Pattern**
The application follows the Model-View-Controller (MVC) architectural pattern:

- **Models**: JPA entities representing database tables (`User`, `Product`, `Order`, etc.)
- **Views**: Thymeleaf templates for dynamic HTML generation
- **Controllers**: REST and web controllers handling HTTP requests
- **Services**: Business logic layer with transaction management
- **Repositories**: Data access layer with Spring Data JPA

### **Security Architecture**
- **Authentication**: JWT-based stateless authentication
- **Authorization**: Role-based access control (RBAC)
- **Password Security**: BCrypt password hashing
- **Email Verification**: Token-based email verification system
- **CSRF Protection**: Cross-site request forgery protection

### **Database Design**
- **Normalized Schema**: Well-structured relational database design
- **Foreign Key Relationships**: Proper referential integrity
- **Indexing**: Optimized database performance
- **Migration Support**: Flyway database migration management

## 🚀 Getting Started

### **Prerequisites**
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Node.js (for frontend dependencies)

### **Installation**

1. **Clone the repository**
   ```bash
   git clone https://github.com/santinomassera1/ecommerceApp.git
   cd ecommerceApp
   ```

2. **Database Setup**
   ```sql
   CREATE DATABASE ecommerce_db;
   ```

3. **Configure Application Properties**
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   ```

4. **Install Dependencies**
   ```bash
   mvn clean install
   npm install
   ```

5. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the Application**
   - Application: http://localhost:8080
   - Admin Panel: http://localhost:8080/admin

### **Default Credentials**
- **Admin User**: admin@vintagevogue.com / admin123
- **Test User**: user@example.com / password123

## 📚 API Documentation

### **Authentication Endpoints**
- `POST /register` - User registration
- `POST /login` - User authentication
- `POST /forgot-password` - Password reset request
- `POST /reset-password` - Password reset confirmation
- `GET /verify-email` - Email verification

### **Product Endpoints**
- `GET /products` - List all products
- `GET /products/{id}` - Get product details
- `POST /products` - Create new product (Admin/Seller)
- `PUT /products/{id}` - Update product (Admin/Seller)
- `DELETE /products/{id}` - Delete product (Admin)

### **Cart Endpoints**
- `GET /cart` - Get user's cart
- `POST /cart/add` - Add item to cart
- `PUT /cart/update` - Update cart item quantity
- `DELETE /cart/remove/{id}` - Remove item from cart

### **Order Endpoints**
- `POST /orders` - Create new order
- `GET /orders` - Get user's orders
- `GET /orders/{id}` - Get order details
- `PUT /orders/{id}/status` - Update order status (Admin)

## 🗄️ Database Schema

### **Core Entities**
- **Users**: User accounts with roles and authentication
- **Products**: Product catalog with categories and images
- **Orders**: Order management with tracking and status
- **Cart**: Shopping cart functionality
- **Categories**: Product categorization system

### **Communication Entities**
- **Messages**: Real-time messaging between users
- **SupportTickets**: Customer support system
- **Notifications**: User notification system

### **Administrative Entities**
- **Ads**: Advertisement and promotional content
- **Comments**: Product reviews and feedback

## 🚀 Deployment

### **Production Configuration**
1. Update database connection settings
2. Configure email service credentials
3. Set up file upload directory
4. Configure security settings
5. Set maintenance mode if needed

### **Environment Variables**
```bash
export SPRING_PROFILES_ACTIVE=production
export DB_URL=jdbc:mysql://your-db-host:3306/ecommerce_db
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export MAIL_USERNAME=your_email@domain.com
export MAIL_PASSWORD=your_app_password
```

### **Docker Deployment** (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/vintagevouge-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### **Development Guidelines**
- Follow Java coding standards
- Write unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


---

<div align="center">
  <p>Made with ❤️ by the Vintage Vogue Team</p>
  <p>© 2024 Vintage Vogue. All rights reserved.</p>
</div>