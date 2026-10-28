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

### Requirements

- [JDK](https://jdk.java.net/)
- [JavaFX SDK](https://openjfx.io/)
- [Git](https://git-scm.com/)

Verify your Java installation:

```powershell
java -version
javac -version
```

### 1. Clone the Repository

```powershell
git clone https://github.com/rayensassiii/Carpooling-Tunisia.git
cd Carpooling-Tunisia
```

### 2. Configure JavaFX

Download and extract the [JavaFX SDK](https://openjfx.io/).

Find the path to its `lib` directory. For example:

```text
C:\Program Files\javafx-sdk-26\lib
```

Replace this example path in the commands below with your actual JavaFX SDK path.

### 3. Compile the Application

```powershell
cd src
javac --module-path "C:\Program Files\javafx-sdk-26\lib" --add-modules javafx.controls -d . com\carpool\*.java
```

### 4. Run the Application

```powershell
java --module-path "C:\Program Files\javafx-sdk-26\lib" --add-modules javafx.controls -cp . com.carpool.CarpoolApp
```

**Note:** These commands assume that the source files are in `src/com/carpool/`, the main class is `CarpoolApp`, and the application requires only `javafx.controls`. Additional dependencies may be needed.

## 🧪 Testing

Launch the application and register an account if registration is available.

If demo accounts are configured, use the credentials provided by the project maintainer.

If the SQLite database is not included in the repository, initialize it according to the project configuration.

## 🔒 Security

- Never commit real passwords or private credentials.
- Use test accounts for demonstrations.
- Store passwords securely using an appropriate hashing method.

## 🎯 Project Goal

Promote accessible and affordable travel between Tunisian cities through a digital carpooling platform.

## 👨‍💻 Author

**Rayen Sassi**

- GitHub: [@rayensassiii](https://github.com/rayensassiii)

---

*Developed as a Java object-oriented programming project.*
