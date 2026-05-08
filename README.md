# University ERP System (Java + Swing)

A full-featured desktop-based University ERP system built using Java, Swing, Maven, and MySQL. The project supports role-based access for Students, Instructors, and Admins, along with course registration, grading workflows, maintenance controls, and authentication management.

---

# Team Members

* Mayank Yadav (2024343)
* Vaishnavi Rai (2024599)

---

* [Project Report](Report.pdf)
* [Testing Plan](TestPlan.pdf)

---

# Tech Stack

* Java 21
* Java Swing
* Maven
* MySQL
* JDBC + HikariCP
* CSV Export Utilities

---

# Features

## Student Features

* View course catalog
* Register/drop courses
* View registered sections
* View completed courses
* View grades and assessment scores
* Export grades as CSV

---

## Instructor Features

* Manage assigned sections
* Create/Edit/Delete assessments
* Enter student scores
* Manage grading slabs
* Preview final grades
* View section statistics
* Export scores and grades

---

## Admin Features

* Add/manage users
* Add/update/delete courses
* Add/update/delete sections
* Assign instructors
* Manage add/drop deadlines
* Toggle maintenance mode

---

# Authentication & Security

* Password hashing
* Failed login tracking
* Account lockout after multiple failed attempts
* Role-based access control
* Instructor-section ownership validation
* Session-based access management

---

# Maintenance Mode

The ERP supports a global maintenance mode.

### Functionality

* Displays a maintenance banner in the UI
* Blocks all database write operations
* Allows safe system administration

All write operations call:

```java id="t6g1vw"
requireWriteAllowed()
```

before modifying the database.

---

# Final Grade Computation

Final grades are computed using weighted assessments.

## Formula

```text id="kns62x"
(score / max_score) × assessment_weight
```

The final percentage is mapped to a letter grade using instructor-defined grading slabs.

---

# Database Architecture

## AUTH_DB

### Tables

* `AUTH_USERS`

Stores:

* usernames
* password hashes
* login metadata
* failed attempts
* account lock status

---

## ERP_DB

### Tables

* `STUDENTS`
* `INSTRUCTORS`
* `COURSES`
* `SECTIONS`
* `ENROLLMENTS`
* `ASSESSMENTS`
* `SCORES`
* `FINAL_GRADES`
* `GRADE_SLABS`
* `ADD_DROP_DEADLINE`
* `SETTINGS`

---

# Project Structure

```text id="e0dg5z"
src/
├── auth/
├── access/
├── service/
├── ui/
├── domain/
├── util/
└── data/
```

---

# UML & Architecture

The system follows a layered architecture:

* UI Layer
* Service Layer
* Access Layer
* Authentication Layer
* Database Layer

Includes:

* Domain diagrams
* Service diagrams
* API diagrams
* Authentication flow diagrams

---

# How to Run

## Prerequisites

* Java 21
* Maven 3.9+
* MySQL
* Windows PowerShell

---

## Database Setup

1. Run the provided MySQL seed scripts
2. Configure database credentials in:

```text id="j4lr7t"
edu.univ.erp.util.DataSourceProvider
```

Example:

```java id="yr4cwo"
cfg.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC");
cfg.setUsername("your_username");
cfg.setPassword("your_password");
```

---

## Run the Application

```bash id="0fx4rr"
mvn clean compile exec:java "-Dexec.mainClass=edu.univ.erp.Main"
```

---

# Highlights

* Multi-role ERP platform
* Fully desktop-based UI using Java Swing
* Role-based access management
* Real-world academic workflows
* Modular layered architecture
* Database-driven backend
* CSV export support
* Maintenance-safe operations
* Secure authentication system

---

# Inspiration

The UI design was inspired by the IIIT Delhi ERP system, including:

* Color scheme
* Navigation layout
* Dashboard structure
* Top bar and logout flow
