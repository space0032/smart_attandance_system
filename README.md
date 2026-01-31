# Smart Attendance System

A comprehensive Java-based smart attendance system with face recognition capabilities using Spring Boot, OpenCV, and MySQL/H2 database.

## Features

- **Face Recognition**: Automatic attendance marking using facial recognition technology powered by OpenCV
- **Real-time Processing**: Instant face detection and student identification from camera feeds
- **Excel Reports**: Export attendance records to Excel format using Apache POI
- **Student Management**: Easy student registration and management with face enrollment
- **Dashboard**: Live attendance dashboard with statistics and analytics
- **REST API**: Complete RESTful API for all operations
- **Responsive UI**: Modern, mobile-friendly interface built with Bootstrap 5

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.1**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Thymeleaf
- **OpenCV 4.7.0** - Face detection and recognition
- **Apache POI 5.2.3** - Excel file generation
- **H2 Database** (Development) / **MySQL** (Production)
- **Bootstrap 5** - Frontend styling
- **Maven** - Build tool

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) MySQL 8.0+ for production deployment

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/space0032/smart_attandance_system.git
cd smart_attandance_system
```

### 2. Build the project

```bash
mvn clean install
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

### Database Configuration

#### Development (H2 - Default)

The application uses H2 in-memory database by default. No configuration needed.

Access H2 Console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:attendancedb`
- Username: `sa`
- Password: (leave empty)

#### Production (MySQL)

Edit `src/main/resources/application.properties`:

```properties
# Comment out H2 settings and uncomment MySQL settings
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### File Upload Configuration

Maximum file size is set to 10MB. Modify in `application.properties` if needed:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Usage

### 1. Access the Application

Open your browser and navigate to `http://localhost:8080`

### 2. Register Students

1. Go to "Register" page
2. Fill in student details (Student ID, Name, Email, Department)
3. Either capture photo using webcam or upload a photo
4. Submit the form

### 3. Mark Attendance

1. Go to "Attendance" page
2. Select classroom
3. Upload a camera feed image (containing student faces)
4. System will automatically detect and recognize faces
5. Attendance is marked for recognized students

### 4. View Attendance

1. Go to "Attendance" page
2. Select classroom and date
3. Click "Load Attendance" to view records
4. Click "Export to Excel" to download attendance report

### 5. Manage Students

- View all registered students on "Students" page
- Search and filter students
- View individual student details
- Delete students if needed

## API Endpoints

### Student Management

- `POST /api/students/register` - Register new student with face image
- `GET /api/students/all` - Get all students
- `GET /api/students/{id}` - Get student by ID
- `PUT /api/students/{id}` - Update student information
- `DELETE /api/students/{id}` - Delete student

### Attendance Management

- `POST /api/attendance/process` - Process camera image and mark attendance
- `GET /api/attendance/list?classroomId={id}&date={date}` - Get attendance records
- `GET /api/attendance/today/{classroomId}` - Get today's attendance
- `GET /api/attendance/export?classroomId={id}&date={date}` - Export to Excel
- `GET /api/attendance/stats?date={date}` - Get attendance statistics

### API Response Format

All API endpoints return responses in this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { }
}
```

## Project Structure

```
src/main/java/com/attendance/
├── SmartAttendanceApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── OpenCVConfig.java
├── controller/
│   ├── AttendanceController.java
│   ├── StudentController.java
│   ├── DashboardController.java
│   └── FaceRecognitionController.java
├── service/
│   ├── AttendanceService.java
│   ├── StudentService.java
│   ├── FaceRecognitionService.java
│   └── ExcelExportService.java
├── repository/
│   ├── StudentRepository.java
│   ├── AttendanceRepository.java
│   └── ClassroomRepository.java
├── model/
│   ├── Student.java
│   ├── Attendance.java
│   └── Classroom.java
├── dto/
│   ├── StudentDTO.java
│   └── ApiResponse.java
└── util/
    ├── FaceDetector.java
    └── ExcelGenerator.java

src/main/resources/
├── application.properties
├── data.sql
├── haarcascade_frontalface_default.xml
└── templates/
    ├── index.html
    ├── dashboard.html
    ├── students.html
    ├── attendance.html
    └── register.html
```

## Sample Data

The application comes with pre-loaded sample data:

### Classrooms
- A101 - CS101 (Computer Science Fundamentals)
- B205 - CS201 (Data Structures and Algorithms)
- C310 - CS301 (Database Management Systems)

### Students
- STU001 - John Doe
- STU002 - Jane Smith
- STU003 - Michael Johnson
- STU004 - Emily Davis
- STU005 - Robert Brown

## Face Recognition

The system uses OpenCV's Haar Cascade classifier for face detection. For production use, consider:

1. **Better Face Recognition Models**: 
   - OpenCV's LBPHFaceRecognizer
   - Dlib's face recognition
   - Deep learning models (FaceNet, ArcFace)

2. **Improved Accuracy**:
   - Multiple face samples per student
   - Better lighting conditions
   - Higher quality cameras
   - Fine-tuned recognition threshold

## Security

- Spring Security is configured for basic authentication
- API endpoints have CSRF protection disabled for testing
- Public access to registration and main pages
- File upload size limits enforced
- Input validation on all forms

## Excel Export

Attendance reports include:
- Student ID
- Full Name
- Email
- Department
- Check-in Time
- Attendance Status

Files are named: `attendance_YYYY-MM-DD.xlsx`

## Troubleshooting

### OpenCV Issues

If you encounter OpenCV loading errors:

1. Ensure OpenCV native library is properly loaded
2. Check `haarcascade_frontalface_default.xml` is in resources folder
3. Verify Java version compatibility

### Database Issues

If database connection fails:

1. Check database credentials in `application.properties`
2. Ensure MySQL is running (if using MySQL)
3. Verify database exists or set `createDatabaseIfNotExist=true`

### Face Detection Issues

If faces are not detected:

1. Ensure good lighting in images
2. Use high-quality camera images
3. Face should be clearly visible and front-facing
4. Adjust recognition threshold if needed

## Docker Deployment (Optional)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/smart-attendance-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
mvn clean package
docker build -t smart-attendance-system .
docker run -p 8080:8080 smart-attendance-system
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Authors

- Smart Attendance Team

## Acknowledgments

- OpenCV for face detection capabilities
- Spring Boot for the robust framework
- Apache POI for Excel generation
- Bootstrap for beautiful UI components

## Support

For issues and questions:
- Open an issue on GitHub
- Contact: support@smartattendance.com

## Roadmap

- [ ] Mobile app for attendance marking
- [ ] Real-time camera feed processing
- [ ] Advanced analytics and reporting
- [ ] Multi-classroom simultaneous processing
- [ ] SMS/Email notifications
- [ ] Integration with Learning Management Systems
- [ ] Support for multiple face recognition algorithms
- [ ] Cloud deployment support