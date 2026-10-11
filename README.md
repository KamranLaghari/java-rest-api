# Java REST API with MySQL

A simple REST API built using **Core Java, JDBC, MySQL, and Java's built-in HTTP server**. This project demonstrates CRUD (Create, Read, Update, Delete) operations for managing student records without using Spring Boot.

## Features

- **GET** – Retrieve all students
- **POST** – Create a new student
- **PUT** – Update an existing student
- **DELETE** – Delete a student
- MySQL database integration using JDBC
- JSON request and response handling
- Built-in Java HTTP server

## Technologies Used

- Java 21
- Core Java
- JDBC
- MySQL
- IntelliJ IDEA
- HTTP REST API

## Database Setup

Create a database named `student_db` and a table named `students`:

```sql
CREATE DATABASE student_db;

USE student_db;

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    age INT
);
```

Configure your MySQL connection details in the `student_db` class.

## Run the Project

1. Install Java 21 and MySQL.
2. Open the project in IntelliJ IDEA.
3. Add the MySQL JDBC driver to your project.
4. Configure the database connection.
5. Run `Main.java`.

The API will start at:

`http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/students` | Get all students |
| POST | `/students` | Create a student |
| PUT | `/students?id=1` | Update a student |
| DELETE | `/students?id=1` | Delete a student |

### Create a Student

**POST** `http://localhost:8080/students`

Request body:

```json
{
  "name": "Kamran",
  "email": "kamran@example.com",
  "age": 22
}
```

### Update a Student

**PUT** `http://localhost:8080/students?id=1`

Request body:

```json
{
  "name": "Kamran Laghari",
  "email": "kamran.updated@example.com",
  "age": 23
}
```

### Delete a Student

**DELETE** `http://localhost:8080/students?id=1`

## Testing

You can test the API using Postman or a browser for GET requests.

## Learning Objectives

- Understand REST API fundamentals
- Practice Java database connectivity with JDBC
- Perform CRUD operations with MySQL
- Handle HTTP methods and JSON data
- Build a basic backend service without a framework

## Author

**Kamran Laghari**

Computer Science Graduate | Java Backend Development

