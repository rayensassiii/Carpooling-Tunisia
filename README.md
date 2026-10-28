# 🇹🇳 Tunisian Carpool

A JavaFX desktop application that connects drivers and passengers across Tunisia. Users can publish trips, search for rides, reserve seats, and manage bookings.

## ✨ Features

- User registration and authentication
- Driver and passenger accounts
- Trip creation and search
- Seat reservations and booking management
- Vehicle and payment management

## 🛠️ Technologies

- **Java** — Application logic
- **JavaFX** — Graphical interface
- **SQLite** — Database
- **JDBC** — Database connectivity

## 🚀 Getting Started

### 1. Requirements

Install the following:

- [JDK](https://jdk.java.net/) — Java Development Kit
- [JavaFX SDK](https://openjfx.io/) — JavaFX libraries
- [Git](https://git-scm.com/) — To clone the repository

Verify that Java is installed:

```powershell
java -version
javac -version
```

### 2. Download the Project

Clone the repository:

```powershell
git clone https://github.com/rayensassi100-ship-it/Carpooling-Tunisia.git
cd Carpooling-Tunisia
```

### 3. Configure JavaFX

Download and extract the JavaFX SDK.

Find the path to its `lib` directory. For example:

```text
C:\Program Files\javafx-sdk-26\lib
```

Replace this example path in the commands below with your actual JavaFX `lib` directory.

### 4. Compile the Application

From the project root, enter the source directory:

```powershell
cd src
```

Compile the Java files:

```powershell
javac --module-path "C:\Program Files\javafx-sdk-26\lib" --add-modules javafx.controls -d . com\carpool\*.java
```

### 5. Run the Application

```powershell
java --module-path "C:\Program Files\javafx-sdk-26\lib" --add-modules javafx.controls -cp . com.carpool.CarpoolApp
```

**Note:** These commands assume that the source files are located in `src/com/carpool/`, that the main class is `CarpoolApp`, and that the application only requires the `javafx.controls` module. Additional JavaFX modules or dependencies may be necessary depending on the implementation.

## 🧪 Testing the Application

Launch the application and register a new account if registration is available.

If demo accounts are provided, use the credentials documented by the project maintainer. Do not use personal credentials.

**Database:** Check the project configuration for the SQLite database location. If the database is not included in the repository, follow the database setup instructions before launching the application.

## 🔒 Security

- Never commit real passwords, private credentials, or personal data.
- Use test accounts when demonstrating the application.
- Store passwords securely using an appropriate password-hashing method.

## 🎯 Project Goal

Promote accessible and affordable travel between Tunisian cities through a simple digital carpooling platform.

## 👨‍💻 Author

**Rayen Sassi**

---

*Developed as a Java object-oriented programming project.*
