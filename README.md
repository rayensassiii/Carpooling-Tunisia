# 🇹🇳 Tunisian Carpool

A JavaFX desktop application for carpooling in Tunisia. It connects drivers and passengers to manage trips, bookings, and payments through a simple graphical interface.


## 🛠️ Technologies

- **Java** — Application logic
- **JavaFX** — Graphical interface
- **SQLite** — Database
- **JDBC** — Database connectivity

## ✨ Main Features

- User authentication
- Driver and passenger roles
- Trip creation and search
- Seat reservations
- Booking and payment management
- Vehicle and trip management

## 🚀 Run the Project

**Requirements:** JDK and JavaFX SDK.

Open CMD and run:

```cmd
cd /d "C:\TunisianCarpool\src"
```

Compile:

```cmd
javac --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls com\carpool\*.java
```

Run:

```cmd
java --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls com.carpool.CarpoolApp
```

Replace `C:\javafx-sdk\lib` with your actual JavaFX SDK path.

## 🇹🇳 Project Goal

Promote convenient and affordable travel between Tunisian cities through a digital carpooling platform.

## 🔐 Security

Passwords should be stored as hashes, and access should be controlled according to user roles.

## 👨‍💻 Author

**Rayen Sassi**

---

*Developed as a Java object-oriented programming project.*
