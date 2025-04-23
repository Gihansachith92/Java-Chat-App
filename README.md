# ChatNest Application

A Java-based chat application with a graphical user interface.

## Project Structure

This is a Maven-based Java project with the following structure:
- `src/main/java`: Contains all Java source files
- `src/main/resources`: Contains configuration files like hibernate.cfg.xml

## How to Run the Application

### Method 1: Using the Batch File (Windows)

Simply double-click the `run.bat` file in the project root directory to start the application.

### Method 2: Using Maven Commands

The application can be run directly from the `Main` class which serves as the entry point.

1. Compile the project:
```
mvn clean compile
```

2. Run the application:
```
mvn exec:java
```

### Method 3: Building and Running a JAR

1. Build the project to create an executable JAR:
```
mvn clean package
```

2. Run the JAR file:
```
java -jar target/ChatApplication-1.0-SNAPSHOT.jar
```

## Features

- User login and registration
- Chat functionality
- Admin panel for administrators
- Profile management

## Default Admin Account

- Email: admin@example.com
- Password: (Contact system administrator)

## Technical Details

- Java Swing for the GUI
- Hibernate for database operations
- RMI for network communication
