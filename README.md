# AutoXpress

## Overview
AutoXpress is a Spring Boot web application designed for buying and selling cars, providing a platform where users can list cars, book test drives, and report issues, while admins manage listings and user activities. Built with Spring Security for role-based access control, it supports three roles: Public (unauthenticated users), User (`ROLE_USER`), and Admin (`ROLE_ADMIN`). The application features a user-friendly interface with Thymeleaf templates, Bootstrap styling, and jQuery for dynamic interactions, alongside a robust backend using JPA for database operations.

## Features
- **Public Access**: Browse car listings (`/cars`), view car details (`/cars/{id}`), access informational pages (`/about`, `/contact`), and register/login (`/register`, `/login`).
- **User Role (`ROLE_USER`)**: Post cars (`/cars/post`), book test drives (`/cars/*/appointment`), submit reports (`/cars/*/report`), manage profile (`/user/profile`), cancel appointments (`/user/appointments/cancel`), and clear notifications (`/user/notifications/clear`).
- **Admin Role (`ROLE_ADMIN`)**: Manage users (`/admin/users/*`), approve/deactivate/sell cars (`/admin/cars/*`), approve/deny test drives (`/admin/appointments/*`), resolve reports (`/admin/reports/resolve`), and update profile (`/admin/profile`).
- **Security**: Role-based access control via Spring Security, password encryption with `BCryptPasswordEncoder`, session management (maximum one session), and CSRF protection.
- **Frontend**: Responsive UI with Bootstrap, FontAwesome icons, and Thymeleaf templates for dynamic rendering.
- **Backend**: JPA for database interactions, with entities like `UserModel`, `CarModel`, `AppointmentModel`, and `AuditLogModel`.

## Prerequisites
- **Java**: JDK 17 or later
- **Maven**: For dependency management
- **MySQL**: Database for storing application data
- **IDE**: Recommended (e.g., Eclipse STS, IntelliJ IDEA)

## Setup Instructions
1. **Clone the Repository**:
   ```
   git clone https://github.com/your-repo/autoxpress.git
   cd autoxpress
   ```

2. **Configure the Database**:
   - Create a MySQL database named `autoxpress_db`.
   - Update the database configuration in `src/main/resources/application.properties`:
     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/autoxpress_db
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```

3. **Build and Run the Application**:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
   - The application will start on `http://localhost:8080`.

4. **Access the Application**:
   - Register a new user at `http://localhost:8080/register`.
   - Log in at `http://localhost:8080/login`.
   - Admins can access the admin dashboard at `http://localhost:8080/admin/dashboard` (requires `ROLE_ADMIN`).

## Usage
- **Public Users**: Browse cars on the `/cars` page, view details, and register/login to access user features.
- **Authenticated Users**:
  - Post a car listing via `/cars/post`.
  - Book a test drive from a car’s detail page.
  - Manage your dashboard at `/user/dashboard`, including cancelling appointments and clearing notifications.
- **Admins**:
  - Access the admin dashboard to manage users, approve car listings, handle test drive appointments, and resolve reports.
  - Edit/delete cars and users via AJAX-powered modals for a seamless experience.

## Project Structure
- **Controllers**: `AuthController.java`, `HomeController.java`, `CarController.java`, `UserController.java`, `AdminController.java` manage request handling.
- **Models**: `UserModel`, `CarModel`, `AppointmentModel`, `NotificationModel`, `ReportModel`, `AuditLogModel` define the data entities.
- **Services**: Handle business logic (e.g., `UserService`, `CarService`, `AuditLogService`).
- **Security**: `SecurityConfig.java` enforces role-based access control and session management.
- **Templates**: Thymeleaf templates (`home.html`, `car-listing.html`, `user-dashboard.html`, `admin-dashboard.html`) for the UI.

## Contributing
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request with your changes. Ensure all tests pass and follow the existing code style.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact
For inquiries, please contact the project maintainers at support@autoxpress.com.