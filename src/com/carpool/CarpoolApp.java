package com.carpool;

import com.carpool.model.*;
import com.carpool.repository.*;
import com.carpool.service.*;
import com.carpool.exception.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

/**
 * Main JavaFX Application for Carpooling Platform - TUNISIA Edition
 * تطبيق النقل التشاركي - تونس
 * Application de Covoiturage - Tunisie
 * 
 * Modern UI with Tunisian theme (Red & White)
 */
public class CarpoolApp extends Application {
    
    // Services
    private UserRepository userRepository;
    private TripRepository tripRepository;
    private ReservationRepository reservationRepository;
    private PaymentRepository paymentRepository;
    private NotificationRepository notificationRepository;
    private RatingRepository ratingRepository;
    
    private AuthService authService;
    private TripService tripService;
    private ReservationService reservationService;
    private PaymentService paymentService;
    private NotificationService notificationService;
    private RatingService ratingService;
    
    // UI Components
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;
    private BorderPane mainLayout;
    private Label statusLabel;
    private Label userInfoLabel;
    
    // Tunisian Colors (Red & White theme)
    private static final String PRIMARY_COLOR = "#E70013";      // Tunisian Red
    private static final String SECONDARY_COLOR = "#C41E3A";    // Dark Red
    private static final String ACCENT_COLOR = "#8B0000";       // Deep Red
    private static final String SUCCESS_COLOR = "#228B22";      // Forest Green
    private static final String WARNING_COLOR = "#DAA520";      // Golden
    private static final String DANGER_COLOR = "#DC143C";       // Crimson
    private static final String BG_COLOR = "#FFF5F5";           // Light Rose White
    private static final String CARD_COLOR = "#FFFFFF";         // Pure White
    private static final String TEXT_COLOR = "#1A1A2E";         // Dark text
    
    // Tunisian Cities
    private static final String[] TUNISIAN_CITIES = {
        "Tunis", "Sousse", "Sfax", "Monastir", "Gabès", "Bizerte", 
        "Kairouan", "Ariana", "Gafsa", "La Marsa", "Hammamet", "Nabeul",
        "Ben Arous", "Kasserine", "Médenine", "Tozeur", "Mahdia", "Béja",
        "Jendouba", "Sidi Bouzid", "Siliana", "Zaghouan", "Kébili", "Tataouine",
        "Djerba", "El Jem", "Tabarka", "Douz", "Zarzis", "Carthage"
    };
    
    // Currency
    private static final String CURRENCY = "TND";
    private static final String CURRENCY_SYMBOL = "د.ت";
    
    // Date format for Tunisia
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize repositories and services
        initializeServices();
        
        // Create sample data
        createSampleData();
        
        // Create scenes
        createLoginScene();
        createMainScene();
        
        // Setup stage - Tunisian branding
        primaryStage.setTitle("🚗 Covoiturage Tunisie - تونس | Partagez vos trajets");
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }
    
    // Helper method to format currency in TND
    private String formatTND(double amount) {
        return String.format("%.3f %s", amount, CURRENCY);
    }
    
    // Helper method to format currency with symbol
    private String formatTNDSymbol(double amount) {
        return String.format("%.3f %s", amount, CURRENCY_SYMBOL);
    }
    
    private void initializeServices() {
        // Initialize repositories
        userRepository = new UserRepository();
        tripRepository = new TripRepository();
        reservationRepository = new ReservationRepository();
        tripRepository.loadFromDatabase(userRepository);
        reservationRepository.loadFromDatabase(userRepository, tripRepository);
        paymentRepository = new PaymentRepository();
        notificationRepository = new NotificationRepository();
        notificationRepository.loadFromDatabase(userRepository);
        ratingRepository = new RatingRepository();
        ratingRepository.loadFromDatabase(userRepository, tripRepository);
        
        // Initialize services
        notificationService = new NotificationService(notificationRepository);
        paymentService = new PaymentService(paymentRepository);
        authService = new AuthService(userRepository, notificationService);
        tripService = new TripService(tripRepository, reservationRepository, notificationService);
        reservationService = new ReservationService(reservationRepository, paymentService, notificationService);
        ratingService = new RatingService(ratingRepository, notificationService);
    }
    
    private void createSampleData() {
        try {
            // Create sample Tunisian drivers
            Driver driver1 = authService.registerDriver("ahmed.benali@email.tn", "pass123", 
                "Ahmed", "Ben Ali", "+21698123456", "TN-12345678");
            driver1.verify();
            
            Vehicle vehicle1 = new Vehicle("Peugeot", "208", 2022, "123 TUN 4567", 
                "Blanc", VehicleType.SEDAN, 4);
            driver1.addVehicle(vehicle1);
            userRepository.save(driver1);
            
            Driver driver2 = authService.registerDriver("fatma.trabelsi@email.tn", "pass123", 
                "Fatma", "Trabelsi", "+21655987654", "TN-87654321");
            driver2.verify();
            
            Vehicle vehicle2 = new Vehicle("Renault", "Symbol", 2021, "456 TUN 7890", 
                "Gris", VehicleType.SEDAN, 4);
            driver2.addVehicle(vehicle2);
            userRepository.save(driver2);
            
            Driver driver3 = authService.registerDriver("mohamed.gharbi@email.tn", "pass123", 
                "Mohamed", "Gharbi", "+21622334455", "TN-11223344");
            driver3.verify();
            
            Vehicle vehicle3 = new Vehicle("Volkswagen", "Caddy", 2023, "789 TUN 1234", 
                "Noir", VehicleType.MINIVAN, 6);
            driver3.addVehicle(vehicle3);
            userRepository.save(driver3);
            
            // Create sample Tunisian passengers
            Passenger passenger1 = authService.registerPassenger("youssef.hammami@email.tn", "pass123",
                "Youssef", "Hammami", "+21697654321");
            PaymentMethod pm1 = new PaymentMethod("4111111111111111", "Youssef Hammami", "12/26", "123");
            passenger1.addPaymentMethod(pm1);
            userRepository.save(passenger1);
            
            Passenger passenger2 = authService.registerPassenger("sarra.mejri@email.tn", "pass123",
                "Sarra", "Mejri", "+21654321098");
            PaymentMethod pm2 = new PaymentMethod("5500000000000004", "Sarra Mejri", "06/27", "456");
            passenger2.addPaymentMethod(pm2);
            userRepository.save(passenger2);
            
            Passenger passenger3 = authService.registerPassenger("khaled.mansour@email.tn", "pass123",
                "Khaled", "Mansour", "+21628765432");
            PaymentMethod pm3 = new PaymentMethod("4000000000000002", "Khaled Mansour", "09/28", "789");
            passenger3.addPaymentMethod(pm3);
            userRepository.save(passenger3);
            
            // Create sample admin
            authService.registerAdmin("admin@covoiturage.tn", "admin123", "Admin", "Système", "+21671000000");
            
            // Create sample trips across Tunisia
            Trip trip1 = tripService.createTrip(driver1, vehicle1, "Tunis", "Sousse",
                LocalDateTime.now().plusDays(1).withHour(7).withMinute(0), 3, 15.000);
            trip1.setDescription("Trajet direct via autoroute A1, climatisation, WiFi disponible");
            
            Trip trip2 = tripService.createTrip(driver1, vehicle1, "Sousse", "Sfax",
                LocalDateTime.now().plusDays(2).withHour(14).withMinute(30), 3, 18.000);
            trip2.setDescription("Route côtière, pause café à Mahdia possible");
            
            Trip trip3 = tripService.createTrip(driver2, vehicle2, "Tunis", "Hammamet",
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0), 3, 10.000);
            trip3.setDescription("Trajet touristique, bagages acceptés");
            trip3.setLuggageAllowed(true);
            
            Trip trip4 = tripService.createTrip(driver2, vehicle2, "Monastir", "Kairouan",
                LocalDateTime.now().plusDays(3).withHour(8).withMinute(0), 3, 12.000);
            trip4.setDescription("Visite de la Grande Mosquée possible");
            
            Trip trip5 = tripService.createTrip(driver3, vehicle3, "Tunis", "Bizerte",
                LocalDateTime.now().plusDays(1).withHour(6).withMinute(30), 5, 8.000);
            trip5.setDescription("Départ tôt le matin, idéal pour le travail");
            
            Trip trip6 = tripService.createTrip(driver3, vehicle3, "Sfax", "Gabès",
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), 5, 20.000);
            trip6.setDescription("Grand véhicule familial, enfants bienvenus");
            trip6.setPetsAllowed(true);
            
            Trip trip7 = tripService.createTrip(driver1, vehicle1, "Nabeul", "Tunis",
                LocalDateTime.now().plusDays(4).withHour(17).withMinute(0), 3, 8.500);
            trip7.setDescription("Retour en fin de journée, musique chill");
            
            Trip trip8 = tripService.createTrip(driver2, vehicle2, "Djerba", "Médenine",
                LocalDateTime.now().plusDays(5).withHour(11).withMinute(0), 3, 7.000);
            trip8.setDescription("Connexion ferry incluse si nécessaire");
            
            System.out.println("✅ Données tunisiennes créées avec succès!");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création des données: " + e.getMessage());
        }
    }
    
    // ==================== LOGIN SCENE ====================
    
    private void createLoginScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, " + PRIMARY_COLOR + ", " + SECONDARY_COLOR + ");");
        
        // Logo and title - Tunisian branding
        Label logoLabel = new Label("🚗 Covoiturage Tunisie");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 42));
        logoLabel.setTextFill(Color.WHITE);
        
        Label tagline = new Label("Partagez vos trajets, économisez ensemble! 🇹🇳");
        tagline.setFont(Font.font("System", 18));
        tagline.setTextFill(Color.WHITE);
        
        Label arabicTagline = new Label("شارك رحلتك - تونس");
        arabicTagline.setFont(Font.font("System", 16));
        arabicTagline.setTextFill(Color.web("#FFCCCC"));
        
        // Login form
        VBox loginBox = createStyledCard();
        loginBox.setMaxWidth(400);
        loginBox.setSpacing(15);
        
        Label loginTitle = new Label("Connexion");
        loginTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        TextField emailField = new TextField();
        emailField.setPromptText("Adresse email");
        styleTextField(emailField);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        styleTextField(passwordField);
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(DANGER_COLOR));
        
        Button loginBtn = createPrimaryButton("Se Connecter");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), 
            passwordField.getText(), errorLabel));
        
        // Quick login buttons for demo - Tunisian accounts
        Label demoLabel = new Label("Connexion rapide (Démo):");
        demoLabel.setStyle("-fx-text-fill: #666;");
        
        HBox demoButtons = new HBox(10);
        demoButtons.setAlignment(Pos.CENTER);
        
        Button demoPassenger = createSecondaryButton("Passager");
        demoPassenger.setOnAction(e -> {
            emailField.setText("youssef.hammami@email.tn");
            passwordField.setText("pass123");
        });
        
        Button demoDriver = createSecondaryButton("Conducteur");
        demoDriver.setOnAction(e -> {
            emailField.setText("ahmed.benali@email.tn");
            passwordField.setText("pass123");
        });
        
        Button demoAdmin = createSecondaryButton("Admin");
        demoAdmin.setOnAction(e -> {
            emailField.setText("admin@covoiturage.tn");
            passwordField.setText("admin123");
        });
        
        demoButtons.getChildren().addAll(demoPassenger, demoDriver, demoAdmin);
        
        Separator sep = new Separator();
        
        Label registerLabel = new Label("Pas encore de compte?");
        Button registerBtn = createLinkButton("S'inscrire");
        registerBtn.setOnAction(e -> showRegisterDialog());
        
        loginBox.getChildren().addAll(
            loginTitle, emailField, passwordField, errorLabel, 
            loginBtn, sep, demoLabel, demoButtons,
            new HBox(5, registerLabel, registerBtn)
        );
        
        root.getChildren().addAll(logoLabel, tagline, arabicTagline, loginBox);
        
        loginScene = new Scene(root, 1000, 700);
    }
    
    private void handleLogin(String email, String password, Label errorLabel) {
        try {
            User user = authService.login(email, password);
            errorLabel.setText("");
            showMainScene();
        } catch (AuthenticationException e) {
            errorLabel.setText(e.getMessage());
        }
    }
    
    private void showRegisterDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Inscription");
        dialog.setHeaderText("Créer un nouveau compte");
        
        ButtonType registerButtonType = new ButtonType("S'inscrire", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Passager", "Conducteur");
        roleCombo.setValue("Passager");
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email (ex: nom@email.tn)");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Téléphone (+216...)");
        phoneField.setText("+216");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe (min 6 caractères)");
        
        TextField licenseField = new TextField();
        licenseField.setPromptText("N° Permis (TN-XXXXXXXX)");
        licenseField.setDisable(true);
        
        roleCombo.setOnAction(e -> licenseField.setDisable(!roleCombo.getValue().equals("Conducteur")));
        
        grid.add(new Label("Type de compte:"), 0, 0);
        grid.add(roleCombo, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Nom:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Téléphone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("Mot de passe:"), 0, 5);
        grid.add(passwordField, 1, 5);
        grid.add(new Label("N° Permis:"), 0, 6);
        grid.add(licenseField, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                try {
                    if (roleCombo.getValue().equals("Conducteur")) {
                        return authService.registerDriver(
                            emailField.getText(), passwordField.getText(),
                            firstNameField.getText(), lastNameField.getText(),
                            phoneField.getText(), licenseField.getText());
                    } else {
                        return authService.registerPassenger(
                            emailField.getText(), passwordField.getText(),
                            firstNameField.getText(), lastNameField.getText(),
                            phoneField.getText());
                    }
                } catch (AuthenticationException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur d'inscription", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> showAlert(Alert.AlertType.INFORMATION, 
            "Succès", "Compte créé avec succès! Veuillez vous connecter."));
    }
    
    // ==================== MAIN SCENE ====================
    
    private void createMainScene() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BG_COLOR + ";");
        
        mainScene = new Scene(mainLayout, 1200, 800);
    }
    
    private void showMainScene() {
        User user = authService.getCurrentUser();
        
        // Create header
        HBox header = createHeader();
        mainLayout.setTop(header);
        
        // Create sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);
        
        // Show default content based on role
        if (user instanceof Admin) {
            showAdminDashboard();
        } else if (user instanceof Driver) {
            showDriverDashboard();
        } else {
            showTripSearch();
        }
        
        primaryStage.setScene(mainScene);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label logo = new Label("🚗 Covoiturage Tunisie 🇹🇳");
        logo.setFont(Font.font("System", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web(PRIMARY_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        User user = authService.getCurrentUser();
        
        // Notifications
        Button notifBtn = new Button("🔔");
        int unread = notificationService.getUnreadCount(user);
        if (unread > 0) {
            notifBtn.setText("🔔 " + unread);
        }
        notifBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18;");
        notifBtn.setOnAction(e -> showNotifications());
        
        String roleDisplay = user instanceof Driver ? "Conducteur" : 
                            user instanceof Admin ? "Admin" : "Passager";
        userInfoLabel = new Label(user.getFullName() + " (" + roleDisplay + ")");
        userInfoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Button logoutBtn = createDangerButton("Déconnexion");
        logoutBtn.setOnAction(e -> handleLogout());
        
        header.getChildren().addAll(logo, spacer, notifBtn, userInfoLabel, logoutBtn);
        
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: white;");
        
        User user = authService.getCurrentUser();
        
        if (user instanceof Driver) {
            sidebar.getChildren().addAll(
                createSidebarSectionLabel("Espace passager"),
                createSidebarButton("🔍 Rechercher trajets", this::showTripSearch),
                createSidebarButton("📋 Mes réservations", this::showMyReservations),
                createSidebarButton("💳 Moyens de paiement", this::showPaymentMethods),
                createSidebarButton("🔔 Notifications", this::showNotifications),
                createSidebarButton("👤 Mon profil", this::showProfile),
                createSidebarSectionLabel("Espace conducteur"),
                createSidebarButton("📊 Tableau de bord", this::showDriverDashboard),
                createSidebarButton("➕ Créer un trajet", this::showCreateTrip),
                createSidebarButton("🚗 Mes trajets", this::showMyTrips),
                createSidebarButton("🚙 Mes véhicules", this::showMyVehicles),
                createSidebarButton("⭐ Mes avis", this::showMyRatings)
            );
        } else if (user instanceof Passenger) {
            sidebar.getChildren().addAll(
                createSidebarButton("🔍 Rechercher trajets", this::showTripSearch),
                createSidebarButton("📋 Mes réservations", this::showMyReservations),
                createSidebarButton("💳 Moyens de paiement", this::showPaymentMethods),
                createSidebarButton("🔔 Notifications", this::showNotifications),
                createSidebarButton("👤 Mon profil", this::showProfile)
            );
        } else if (user instanceof Admin) {
            sidebar.getChildren().addAll(
                createSidebarButton("📊 Tableau de bord", this::showAdminDashboard),
                createSidebarButton("👥 Utilisateurs", this::showUserManagement),
                createSidebarButton("🚗 Tous les trajets", this::showAllTrips),
                createSidebarButton("📋 Réservations", this::showAllReservations),
                createSidebarButton("💰 Paiements", this::showAllPayments)
            );
        }
        
        return sidebar;
    }
    
    private Button createSidebarButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 14; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Label createSidebarSectionLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(10, 15, 4, 15));
        label.setStyle("-fx-text-fill: #999; -fx-font-size: 12; -fx-font-weight: bold;");
        return label;
    }
    
    // ==================== PASSENGER VIEWS ====================
    
    private void showTripSearch() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("🔍 Trouvez votre trajet en Tunisie");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Search form
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(20));
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 10; -fx-border-width: 2;");
        
        ComboBox<String> fromField = new ComboBox<>();
        fromField.getItems().addAll(TUNISIAN_CITIES);
        fromField.setPromptText("Ville de départ");
        fromField.setPrefWidth(180);
        fromField.setEditable(true);
        
        Label arrowLabel = new Label("→");
        arrowLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        arrowLabel.setTextFill(Color.web(PRIMARY_COLOR));
        
        ComboBox<String> toField = new ComboBox<>();
        toField.getItems().addAll(TUNISIAN_CITIES);
        toField.setPromptText("Ville d'arrivée");
        toField.setPrefWidth(180);
        toField.setEditable(true);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPromptText("Date");
        
        Button searchBtn = createPrimaryButton("🔍 Rechercher");
        
        searchBox.getChildren().addAll(fromField, arrowLabel, toField, datePicker, searchBtn);
        
        // Results
        VBox resultsBox = new VBox(15);
        
        searchBtn.setOnAction(e -> {
            String from = fromField.getValue() != null ? fromField.getValue() : "";
            String to = toField.getValue() != null ? toField.getValue() : "";
            List<Trip> trips = tripService.searchTrips(from, to, datePicker.getValue());
            displayTripResults(trips, resultsBox);
        });
        
        // Show all available trips initially
        List<Trip> availableTrips = tripService.getAvailableTrips();
        displayTripResults(availableTrips, resultsBox);
        
        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        content.getChildren().addAll(title, searchBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }
    
    private void displayTripResults(List<Trip> trips, VBox container) {
        container.getChildren().clear();
        
        if (trips.isEmpty()) {
            Label noResults = new Label("🔍 Aucun trajet trouvé. Essayez d'autres critères de recherche.");
            noResults.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
            container.getChildren().add(noResults);
            return;
        }
        
        Label resultsLabel = new Label(trips.size() + " trajet(s) trouvé(s)");
        resultsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
        container.getChildren().add(resultsLabel);
        
        for (Trip trip : trips) {
            HBox tripCard = createTripCard(trip);
            container.getChildren().add(tripCard);
        }
    }
    
    private HBox createTripCard(Trip trip) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                     "-fx-border-color: #FFE4E4; -fx-border-radius: 10;");
        
        // Left: Driver info and rating
        VBox driverBox = new VBox(5);
        driverBox.setPrefWidth(150);
        
        Label driverName = new Label(trip.getDriver().getFullName());
        driverName.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        double rating = trip.getDriver().getAverageRating();
        Label ratingLabel = new Label(rating > 0 ? 
            String.format("⭐ %.1f (%d avis)", rating, trip.getDriver().getRatingCount()) : 
            "Pas encore noté");
        ratingLabel.setStyle("-fx-text-fill: #666;");
        
        if (trip.getDriver().isVerified()) {
            Label verifiedLabel = new Label("✓ Vérifié");
            verifiedLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
            driverBox.getChildren().addAll(driverName, ratingLabel, verifiedLabel);
        } else {
            driverBox.getChildren().addAll(driverName, ratingLabel);
        }
        
        // Center: Trip details
        VBox tripDetails = new VBox(8);
        HBox.setHgrow(tripDetails, Priority.ALWAYS);
        
        HBox routeBox = new HBox(10);
        Label fromLabel = new Label(trip.getDepartureCity());
        fromLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        fromLabel.setTextFill(Color.web(PRIMARY_COLOR));
        Label arrow = new Label("→");
        arrow.setFont(Font.font("System", 18));
        Label toLabel = new Label(trip.getArrivalCity());
        toLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        toLabel.setTextFill(Color.web(SECONDARY_COLOR));
        routeBox.getChildren().addAll(fromLabel, arrow, toLabel);
        
        Label dateLabel = new Label("🕐 " + trip.getDepartureTime().format(DATETIME_FORMAT));
        dateLabel.setStyle("-fx-text-fill: #666;");
        
        Label vehicleLabel = new Label("🚗 " + trip.getVehicle().getShortDescription());
        vehicleLabel.setStyle("-fx-text-fill: #666;");
        
        HBox features = new HBox(10);
        if (trip.isPetsAllowed()) features.getChildren().add(new Label("🐾 Animaux OK"));
        if (trip.isLuggageAllowed()) features.getChildren().add(new Label("🧳 Bagages"));
        if (!trip.isSmokingAllowed()) features.getChildren().add(new Label("🚭 Non-fumeur"));
        
        tripDetails.getChildren().addAll(routeBox, dateLabel, vehicleLabel, features);
        
        // Right: Price and book button
        VBox priceBox = new VBox(10);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        priceBox.setPrefWidth(130);
        
        Label priceLabel = new Label(formatTND(trip.getPricePerSeat()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        priceLabel.setTextFill(Color.web(PRIMARY_COLOR));
        
        Label perSeatLabel = new Label("par place");
        perSeatLabel.setStyle("-fx-text-fill: #666;");
        
        Label seatsLabel = new Label(trip.getAvailableSeats() + " places restantes");
        seatsLabel.setStyle("-fx-text-fill: " + 
            (trip.getAvailableSeats() <= 2 ? WARNING_COLOR : "#666") + ";");
        
        Button bookBtn = createPrimaryButton("Book Now");
        bookBtn.setOnAction(e -> showBookingDialog(trip));
        
        if (trip.isFull()) {
            bookBtn.setDisable(true);
            bookBtn.setText("Complet");
        }
        
        priceBox.getChildren().addAll(priceLabel, perSeatLabel, seatsLabel, bookBtn);
        
        card.getChildren().addAll(driverBox, tripDetails, priceBox);
        
        return card;
    }
    
    private void showBookingDialog(Trip trip) {
        User user = authService.getCurrentUser();
        
        if (!(user instanceof Passenger)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", 
                "Seuls les passagers peuvent réserver. Connectez-vous en tant que passager.");
            return;
        }
        
        Passenger passenger = (Passenger) user;
        
        if (passenger.getPaymentMethods().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Paiement requis", 
                "Veuillez ajouter un moyen de paiement avant de réserver.");
            showPaymentMethods();
            return;
        }
        
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Réserver un trajet");
        dialog.setHeaderText("Réservez votre place: " + trip.getRoute());
        
        ButtonType bookButtonType = new ButtonType("Confirmer la réservation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        // Trip info
        grid.add(new Label("Trajet:"), 0, 0);
        grid.add(new Label(trip.getRoute()), 1, 0);
        
        grid.add(new Label("Date:"), 0, 1);
        grid.add(new Label(trip.getDepartureTime().format(DATETIME_FORMAT)), 1, 1);
        
        grid.add(new Label("Conducteur:"), 0, 2);
        grid.add(new Label(trip.getDriver().getFullName()), 1, 2);
        
        grid.add(new Label("Prix par place:"), 0, 3);
        grid.add(new Label(formatTND(trip.getPricePerSeat())), 1, 3);
        
        // Seat selection
        Spinner<Integer> seatSpinner = new Spinner<>(1, trip.getAvailableSeats(), 1);
        grid.add(new Label("Nombre de places:"), 0, 4);
        grid.add(seatSpinner, 1, 4);
        
        // Total price
        Label totalLabel = new Label(formatTND(trip.getPricePerSeat()));
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        seatSpinner.valueProperty().addListener((obs, old, newVal) -> 
            totalLabel.setText(formatTND(trip.getPricePerSeat() * newVal)));
        
        grid.add(new Label("Total:"), 0, 5);
        grid.add(totalLabel, 1, 5);
        
        // Payment method
        ComboBox<PaymentMethod> paymentCombo = new ComboBox<>();
        paymentCombo.getItems().addAll(passenger.getPaymentMethods());
        paymentCombo.setValue(passenger.getDefaultPaymentMethod());
        paymentCombo.setCellFactory(p -> new ListCell<PaymentMethod>() {
            @Override
            protected void updateItem(PaymentMethod item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayString());
            }
        });
        paymentCombo.setButtonCell(paymentCombo.getCellFactory().call(null));
        
        grid.add(new Label("Paiement:"), 0, 6);
        grid.add(paymentCombo, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                try {
                    Reservation reservation = reservationService.createReservation(
                        passenger, trip, seatSpinner.getValue(), paymentCombo.getValue());
                    
                    // Réservation reste en attente - le conducteur doit confirmer
                    // reservationService.confirmReservation(reservation); // REMOVED
                    
                    return reservation;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de réservation", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(r -> {
            showAlert(Alert.AlertType.INFORMATION, "Demande envoyée!", 
                "Votre demande de réservation a été envoyée au conducteur.\n" +
                "N° de réservation: " + r.getId().substring(0, 8) + "\n" +
                "Montant: " + formatTND(r.getTotalPrice()) + "\n\n" +
                "⏳ En attente de confirmation du conducteur...");
            showMyReservations();
        });
    }
    
    private void showMyReservations() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Passenger)) return;
        
        Passenger passenger = (Passenger) user;
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("📋 Mes Réservations");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));

        TabPane reservationsTabs = new TabPane();
        reservationsTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab pendingTab = new Tab("En attente");
        pendingTab.setContent(createPassengerReservationsPane(
            getPassengerReservationsByState(passenger, ReservationViewFilter.PENDING),
            "Aucune réservation en attente. Trouvez un trajet pour commencer!"
        ));

        Tab completedTab = new Tab("Terminées");
        completedTab.setContent(createPassengerReservationsPane(
            getPassengerReservationsByState(passenger, ReservationViewFilter.COMPLETED),
            "Aucune réservation terminée"
        ));

        Tab cancelledTab = new Tab("Annulées");
        cancelledTab.setContent(createPassengerReservationsPane(
            getPassengerReservationsByState(passenger, ReservationViewFilter.CANCELLED),
            "Aucune réservation annulée"
        ));

        reservationsTabs.getTabs().addAll(pendingTab, completedTab, cancelledTab);

        content.getChildren().addAll(title, reservationsTabs);
        VBox.setVgrow(reservationsTabs, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }

    private enum ReservationViewFilter {
        PENDING,
        COMPLETED,
        CANCELLED
    }

    private List<Reservation> getPassengerReservationsByState(Passenger passenger, ReservationViewFilter filter) {
        List<Reservation> result = new java.util.ArrayList<>();
        for (Reservation r : passenger.getReservations()) {
            if (matchesReservationFilter(r, filter)) {
                result.add(r);
            }
        }
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }

    private boolean matchesReservationFilter(Reservation reservation, ReservationViewFilter filter) {
        return switch (filter) {
            case PENDING -> reservation.getStatus() == ReservationStatus.PENDING ||
                (reservation.getStatus() == ReservationStatus.CONFIRMED && !reservation.getTrip().isCompleted());
            case COMPLETED -> isReservationCompletedForView(reservation);
            case CANCELLED -> reservation.getStatus() == ReservationStatus.CANCELLED;
        };
    }

    private boolean isReservationCompletedForView(Reservation reservation) {
        return reservation.getStatus() == ReservationStatus.COMPLETED ||
            (reservation.getTrip().isCompleted() && reservation.getStatus() == ReservationStatus.CONFIRMED);
    }

    private ScrollPane createPassengerReservationsPane(List<Reservation> reservations, String emptyMessage) {
        VBox reservationsBox = new VBox(15);

        if (reservations.isEmpty()) {
            Label noBookings = new Label(emptyMessage);
            noBookings.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
            reservationsBox.getChildren().add(noBookings);
        } else {
            for (Reservation r : reservations) {
                reservationsBox.getChildren().add(createReservationCard(r));
            }
        }

        ScrollPane scrollPane = new ScrollPane(reservationsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private HBox createReservationCard(Reservation r) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
        
        Trip trip = r.getTrip();
        boolean tripCompleted = trip.isCompleted();
        boolean completedForDisplay = r.getStatus() == ReservationStatus.COMPLETED ||
            (tripCompleted && r.getStatus() == ReservationStatus.CONFIRMED);
        ReservationStatus displayStatus = completedForDisplay ? ReservationStatus.COMPLETED : r.getStatus();
        
        // Status indicator
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPrefWidth(100);
        
        String statusColor = switch (displayStatus) {
            case CONFIRMED -> SUCCESS_COLOR;
            case PENDING -> WARNING_COLOR;
            case CANCELLED -> DANGER_COLOR;
            case COMPLETED -> PRIMARY_COLOR;
        };
        
        String statusText = switch (displayStatus) {
            case CONFIRMED -> "Confirmé";
            case PENDING -> "En attente";
            case CANCELLED -> "Annulé";
            case COMPLETED -> "Terminé";
        };
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
        statusBox.getChildren().add(statusLabel);
        
        // Trip details
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label routeLabel = new Label(trip.getRoute());
        routeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Label dateLabel = new Label("🕐 " + trip.getDepartureTime().format(DATETIME_FORMAT));
        
        Label seatsLabel = new Label("👤 " + r.getSeatsBooked() + " place(s) - " + formatTND(r.getTotalPrice()));
        
        Label driverLabel = new Label("🚗 " + trip.getDriver().getFullName());
        
        details.getChildren().addAll(routeLabel, dateLabel, seatsLabel, driverLabel);
        
        // Actions
        VBox actions = new VBox(5);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        if (completedForDisplay) {
            Button rateBtn = createPrimaryButton("Noter");
            rateBtn.setOnAction(e -> showRateDialog(r));
            actions.getChildren().add(rateBtn);
        } else if (r.getStatus() == ReservationStatus.CONFIRMED && !tripCompleted) {
            Button cancelBtn = createDangerButton("Annuler");
            cancelBtn.setOnAction(e -> handleCancelReservation(r));
            actions.getChildren().add(cancelBtn);
        }
        
        card.getChildren().addAll(statusBox, details, actions);
        
        return card;
    }
    
    private void handleCancelReservation(Reservation reservation) {
        double refund = reservation.calculateRefundAmount();
        String message = "Êtes-vous sûr de vouloir annuler cette réservation?\n" +
            (refund > 0 ? "Vous recevrez un remboursement de " + formatTND(refund) : 
             "Aucun remboursement disponible (annulation tardive)");
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, 
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Annuler la réservation");
        alert.setHeaderText("Confirmer l'annulation");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.cancelReservation(reservation);
                    showAlert(Alert.AlertType.INFORMATION, "Annulé", 
                        "Votre réservation a été annulée.");
                    showMyReservations();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }
    
    private void showRateDialog(Reservation reservation) {
        Trip trip = reservation.getTrip();
        Driver driver = trip.getDriver();
        Passenger passenger = reservation.getPassenger();
        
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Noter votre trajet");
        dialog.setHeaderText("Comment était votre trajet avec " + driver.getFullName() + "?");
        
        ButtonType submitType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Star rating - Fixed version
        HBox starsBox = new HBox(5);
        starsBox.setAlignment(Pos.CENTER);
        
        int[] selectedRating = {5};
        Button[] starButtons = new Button[5];
        
        // Helper to update star display
        Runnable updateStars = () -> {
            for (int j = 0; j < 5; j++) {
                if (j < selectedRating[0]) {
                    starButtons[j].setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 28; -fx-cursor: hand;");
                } else {
                    starButtons[j].setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-font-size: 28; -fx-cursor: hand;");
                }
            }
        };
        
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            Button star = new Button("★");
            starButtons[i] = star;
            star.setOnAction(e -> {
                selectedRating[0] = rating;
                updateStars.run();
            });
            starsBox.getChildren().add(star);
        }
        
        // Initialize with 5 stars selected
        updateStars.run();
        
        // Rating label
        Label ratingLabel = new Label("5 étoiles - Excellent!");
        ratingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");
        
        // Update label when rating changes
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            starButtons[i].setOnAction(e -> {
                selectedRating[0] = rating;
                updateStars.run();
                String text = switch(rating) {
                    case 1 -> "1 étoile - Très mauvais";
                    case 2 -> "2 étoiles - Mauvais";
                    case 3 -> "3 étoiles - Correct";
                    case 4 -> "4 étoiles - Bien";
                    case 5 -> "5 étoiles - Excellent!";
                    default -> "";
                };
                ratingLabel.setText(text);
            });
        }
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Écrivez un commentaire (optionnel)");
        commentArea.setPrefRowCount(3);
        
        content.getChildren().addAll(
            new Label("Notez votre expérience:"),
            starsBox,
            ratingLabel,
            new Label("Commentaire:"),
            commentArea
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitType) {
                try {
                    return ratingService.rateDriver(passenger, driver, trip, 
                        selectedRating[0], commentArea.getText());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(rating -> {
            showAlert(Alert.AlertType.INFORMATION, "Merci!", 
                "Votre note a été soumise.");
        });
    }
    
    private void showPaymentMethods() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Passenger)) return;
        
        Passenger passenger = (Passenger) user;
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("💳 Moyens de Paiement");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        Button addBtn = createPrimaryButton("+ Ajouter une carte");
        addBtn.setOnAction(e -> showAddPaymentDialog(passenger));
        
        VBox cardsBox = new VBox(15);
        
        for (PaymentMethod pm : passenger.getPaymentMethods()) {
            HBox card = new HBox(15);
            card.setPadding(new Insets(15));
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
            
            Label cardIcon = new Label("💳");
            cardIcon.setFont(Font.font("System", 24));
            
            VBox cardDetails = new VBox(3);
            HBox.setHgrow(cardDetails, Priority.ALWAYS);
            
            Label cardNumber = new Label(pm.getMaskedCardNumber());
            cardNumber.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            Label cardInfo = new Label(pm.getCardType() + " • Expire " + pm.getExpiryDate());
            cardInfo.setStyle("-fx-text-fill: #666;");
            
            cardDetails.getChildren().addAll(cardNumber, cardInfo);
            
            Label defaultLabel = new Label(pm.isDefault() ? "Par défaut" : "");
            defaultLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
            
            Button removeBtn = createDangerButton("Supprimer");
            removeBtn.setOnAction(e -> {
                passenger.removePaymentMethod(pm);
                userRepository.save(passenger);
                showPaymentMethods();
            });
            
            card.getChildren().addAll(cardIcon, cardDetails, defaultLabel, removeBtn);
            cardsBox.getChildren().add(card);
        }
        
        if (passenger.getPaymentMethods().isEmpty()) {
            Label noCards = new Label("Aucun moyen de paiement. Ajoutez-en un pour réserver.");
            noCards.setStyle("-fx-text-fill: #666;");
            cardsBox.getChildren().add(noCards);
        }
        
        content.getChildren().addAll(title, addBtn, cardsBox);
        
        mainLayout.setCenter(content);
    }
    
    private void showAddPaymentDialog(Passenger passenger) {
        Dialog<PaymentMethod> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un moyen de paiement");
        dialog.setHeaderText("Entrez les détails de votre carte");
        
        ButtonType addType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField cardNumber = new TextField();
        cardNumber.setPromptText("Numéro de carte");
        
        TextField holderName = new TextField();
        holderName.setPromptText("Nom du titulaire");
        
        TextField expiry = new TextField();
        expiry.setPromptText("MM/AA");
        
        TextField cvv = new TextField();
        cvv.setPromptText("CVV");
        
        grid.add(new Label("N° de carte:"), 0, 0);
        grid.add(cardNumber, 1, 0);
        grid.add(new Label("Titulaire:"), 0, 1);
        grid.add(holderName, 1, 1);
        grid.add(new Label("Expiration:"), 0, 2);
        grid.add(expiry, 1, 2);
        grid.add(new Label("CVV:"), 0, 3);
        grid.add(cvv, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addType) {
                PaymentMethod pm = new PaymentMethod(
                    cardNumber.getText(), holderName.getText(), 
                    expiry.getText(), cvv.getText());
                
                if (pm.isValid()) {
                    passenger.addPaymentMethod(pm);
                    return pm;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Carte invalide", 
                        "Veuillez vérifier les détails de votre carte.");
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(pm -> {
            userRepository.save(passenger);
            showAlert(Alert.AlertType.INFORMATION, "Carte ajoutée", 
                "Votre moyen de paiement a été ajouté.");
            showPaymentMethods();
        });
    }
    
    // ==================== DRIVER VIEWS ====================
    
    private void showDriverDashboard() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Driver)) return;
        
        Driver driver = (Driver) user;
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(25));
        
        Label title = new Label("📊 Tableau de Bord Conducteur");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Stats cards
        HBox statsBox = new HBox(20);
        
        VBox completedCard = createStatCard("Trajets terminés", 
            String.valueOf(driver.getCompletedTripsCount()), SUCCESS_COLOR);
        
        VBox activeCard = createStatCard("Trajets actifs", 
            String.valueOf(driver.getActiveTrips().size()), PRIMARY_COLOR);
        
        VBox earningsCard = createStatCard("Gains totaux", 
            formatTND(driver.getTotalEarnings()), WARNING_COLOR);
        
        VBox ratingCard = createStatCard("Note moyenne", 
            driver.getAverageRating() > 0 ? 
                String.format("%.1f ⭐", driver.getAverageRating()) : "N/A", 
            PRIMARY_COLOR);
        
        statsBox.getChildren().addAll(completedCard, activeCard, earningsCard, ratingCard);
        
        // Verification status
        HBox verificationBox = new HBox(10);
        verificationBox.setPadding(new Insets(15));
        verificationBox.setAlignment(Pos.CENTER_LEFT);
        
        if (driver.isVerified()) {
            verificationBox.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 8;");
            Label verifiedLabel = new Label("✓ Votre compte est vérifié");
            verifiedLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
            verificationBox.getChildren().add(verifiedLabel);
        } else {
            verificationBox.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 8;");
            Label pendingLabel = new Label("⏳ Vérification en cours - Contactez l'admin");
            pendingLabel.setStyle("-fx-text-fill: #856404;");
            verificationBox.getChildren().add(pendingLabel);
        }
        
        // Pending reservations
        Label pendingTitle = new Label("Demandes de réservation en attente");
        pendingTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        VBox pendingBox = new VBox(10);
        
        for (Trip trip : driver.getActiveTrips()) {
            List<Reservation> pending = reservationService.getPendingReservations(trip);
            for (Reservation r : pending) {
                pendingBox.getChildren().add(createPendingReservationCard(r));
            }
        }
        
        if (pendingBox.getChildren().isEmpty()) {
            pendingBox.getChildren().add(new Label("Aucune demande en attente"));
        }
        
        content.getChildren().addAll(title, statsBox, verificationBox, pendingTitle, pendingBox);
        
        mainLayout.setCenter(content);
    }
    
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(color));
        
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-text-fill: #666;");
        
        card.getChildren().addAll(valueLabel, textLabel);
        
        return card;
    }
    
    private HBox createPendingReservationCard(Reservation r) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 8;");
        
        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label passengerLabel = new Label(r.getPassenger().getFullName() + 
            " demande " + r.getSeatsBooked() + " place(s)");
        passengerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label tripLabel = new Label(r.getTrip().getRoute() + " - " + 
            r.getTrip().getDepartureTime().format(DATETIME_FORMAT));
        
        details.getChildren().addAll(passengerLabel, tripLabel);
        
        Button acceptBtn = createSuccessButton("Accepter");
        acceptBtn.setOnAction(e -> {
            try {
                reservationService.confirmReservation(r);
                showDriverDashboard();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });
        
        Button rejectBtn = createDangerButton("Refuser");
        rejectBtn.setOnAction(e -> {
            try {
                reservationService.cancelReservation(r);
                showDriverDashboard();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });
        
        card.getChildren().addAll(details, acceptBtn, rejectBtn);
        
        return card;
    }
    
    private void showCreateTrip() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Driver)) return;
        
        Driver driver = (Driver) user;

        if (!driver.isVerified()) {
            showAlert(Alert.AlertType.WARNING, "Compte non vérifié",
                "Votre compte conducteur doit être vérifié par l'admin avant de publier des trajets.");
            return;
        }
        
        if (driver.getVehicles().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun véhicule", 
                "Veuillez ajouter un véhicule avant de créer un trajet.");
            showMyVehicles();
            return;
        }
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setMaxWidth(600);
        
        Label title = new Label("➕ Créer un nouveau trajet");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        VBox formCard = createStyledCard();
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        
        ComboBox<String> fromField = new ComboBox<>();
        fromField.getItems().addAll(TUNISIAN_CITIES);
        fromField.setPromptText("Ville de départ");
        fromField.setEditable(true);
        fromField.setPrefWidth(200);
        
        ComboBox<String> toField = new ComboBox<>();
        toField.getItems().addAll(TUNISIAN_CITIES);
        toField.setPromptText("Ville d'arrivée");
        toField.setEditable(true);
        toField.setPrefWidth(200);
        
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 8);
        Spinner<Integer> minSpinner = new Spinner<>(0, 59, 0, 15);
        
        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.getItems().addAll(driver.getVehicles());
        vehicleCombo.setValue(driver.getPrimaryVehicle());
        vehicleCombo.setCellFactory(p -> new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getShortDescription());
            }
        });
        vehicleCombo.setButtonCell(vehicleCombo.getCellFactory().call(null));
        
        Spinner<Integer> seatsSpinner = new Spinner<>(1, 8, 3);
        vehicleCombo.setOnAction(e -> {
            if (vehicleCombo.getValue() != null) {
                seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    1, vehicleCombo.getValue().getSeats(), 
                    Math.min(seatsSpinner.getValue(), vehicleCombo.getValue().getSeats())));
            }
        });
        
        TextField priceField = new TextField("15.000");
        styleTextField(priceField);
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description du trajet (optionnel)");
        descArea.setPrefRowCount(2);
        
        CheckBox petsCheck = new CheckBox("Animaux acceptés");
        CheckBox smokingCheck = new CheckBox("Fumeurs acceptés");
        
        form.add(new Label("De:"), 0, 0);
        form.add(fromField, 1, 0);
        form.add(new Label("Vers:"), 0, 1);
        form.add(toField, 1, 1);
        form.add(new Label("Date:"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Heure:"), 0, 3);
        form.add(new HBox(5, hourSpinner, new Label(":"), minSpinner), 1, 3);
        form.add(new Label("Véhicule:"), 0, 4);
        form.add(vehicleCombo, 1, 4);
        form.add(new Label("Places:"), 0, 5);
        form.add(seatsSpinner, 1, 5);
        form.add(new Label("Prix (TND):"), 0, 6);
        form.add(priceField, 1, 6);
        form.add(new Label("Description:"), 0, 7);
        form.add(descArea, 1, 7);
        form.add(new Label("Options:"), 0, 8);
        form.add(new VBox(5, petsCheck, smokingCheck), 1, 8);
        
        Button createBtn = createPrimaryButton("Créer le trajet");
        createBtn.setOnAction(e -> {
            try {
                LocalDateTime departure = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.of(hourSpinner.getValue(), minSpinner.getValue()));
                
                double price = Double.parseDouble(priceField.getText().replace(",", "."));
                
                String from = fromField.getValue() != null ? fromField.getValue() : "";
                String to = toField.getValue() != null ? toField.getValue() : "";
                
                Trip trip = tripService.createTrip(driver, vehicleCombo.getValue(),
                    from, to, departure,
                    seatsSpinner.getValue(), price);
                
                trip.setDescription(descArea.getText());
                trip.setPetsAllowed(petsCheck.isSelected());
                trip.setSmokingAllowed(smokingCheck.isSelected());
                
                showAlert(Alert.AlertType.INFORMATION, "Trajet créé", 
                    "Votre trajet a été publié!");
                showMyTrips();
                
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });
        
        formCard.getChildren().addAll(form, createBtn);
        content.getChildren().addAll(title, formCard);
        
        mainLayout.setCenter(content);
    }
    
    private void showMyTrips() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Driver)) return;
        
        Driver driver = (Driver) user;
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("🚗 Mes Trajets");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        Button createBtn = createPrimaryButton("+ Créer un trajet");
        createBtn.setOnAction(e -> showCreateTrip());
        
        TabPane tripsTabs = new TabPane();
        tripsTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab pendingTab = new Tab("En attente");
        pendingTab.setContent(createDriverTripsPane(getDriverTripsByState(driver, false),
            "Aucun trajet en attente"));

        Tab completedTab = new Tab("Terminés");
        completedTab.setContent(createDriverTripsPane(getDriverTripsByState(driver, true),
            "Aucun trajet terminé"));

        tripsTabs.getTabs().addAll(pendingTab, completedTab);

        content.getChildren().addAll(title, createBtn, tripsTabs);
        VBox.setVgrow(tripsTabs, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }

    private List<Trip> getDriverTripsByState(Driver driver, boolean completed) {
        List<Trip> result = new java.util.ArrayList<>();
        for (Trip trip : driver.getTrips()) {
            if (completed) {
                if (trip.getStatus() == TripStatus.COMPLETED) {
                    result.add(trip);
                }
            } else if (trip.getStatus() != TripStatus.COMPLETED && trip.getStatus() != TripStatus.CANCELLED) {
                result.add(trip);
            }
        }
        result.sort((a, b) -> b.getDepartureTime().compareTo(a.getDepartureTime()));
        return result;
    }

    private ScrollPane createDriverTripsPane(List<Trip> trips, String emptyMessage) {
        VBox tripsBox = new VBox(15);

        for (Trip trip : trips) {
            tripsBox.getChildren().add(createDriverTripCard(trip));
        }

        if (tripsBox.getChildren().isEmpty()) {
            tripsBox.getChildren().add(new Label(emptyMessage));
        }

        ScrollPane scrollPane = new ScrollPane(tripsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private HBox createDriverTripCard(Trip trip) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
        
        // Status
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPrefWidth(100);
        
        String statusColor = switch (trip.getStatus()) {
            case OPEN -> SUCCESS_COLOR;
            case FULL -> WARNING_COLOR;
            case IN_PROGRESS -> PRIMARY_COLOR;
            case COMPLETED -> "#666";
            case CANCELLED -> DANGER_COLOR;
        };
        
        String statusText = switch (trip.getStatus()) {
            case OPEN -> "Ouvert";
            case FULL -> "Complet";
            case IN_PROGRESS -> "En cours";
            case COMPLETED -> "Terminé";
            case CANCELLED -> "Annulé";
        };
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
        statusBox.getChildren().add(statusLabel);
        
        // Details
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label routeLabel = new Label(trip.getRoute());
        routeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Label dateLabel = new Label("🕐 " + trip.getDepartureTime().format(DATETIME_FORMAT));
        
        Label seatsLabel = new Label("👥 " + trip.getBookedSeats() + "/" + 
            trip.getTotalSeats() + " réservées | " + formatTND(trip.getPricePerSeat()) + "/place");
        
        details.getChildren().addAll(routeLabel, dateLabel, seatsLabel);
        
        // Actions
        VBox actions = new VBox(5);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        if (trip.isOpen() || trip.getStatus() == TripStatus.FULL) {
            Button viewBtn = createSecondaryButton("Voir passagers");
            viewBtn.setOnAction(e -> showTripPassengers(trip));
            actions.getChildren().add(viewBtn);
            
            if (trip.canCancel()) {
                Button cancelBtn = createDangerButton("Annuler");
                cancelBtn.setOnAction(e -> handleCancelTrip(trip));
                actions.getChildren().add(cancelBtn);
            }
            
            if (trip.getStatus() != TripStatus.IN_PROGRESS) {
                Button startBtn = createPrimaryButton("Démarrer");
                startBtn.setOnAction(e -> {
                    try {
                        tripService.closeTrip(trip);
                        showMyTrips();
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                    }
                });
                actions.getChildren().add(startBtn);
            }
        }
        
        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            Button completeBtn = createSuccessButton("Terminer");
            completeBtn.setOnAction(e -> {
                try {
                    tripService.completeTrip(trip);
                    showMyTrips();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            });
            actions.getChildren().add(completeBtn);
        }
        
        card.getChildren().addAll(statusBox, details, actions);
        
        return card;
    }
    
    private void showTripPassengers(Trip trip) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Passagers");
        alert.setHeaderText("Passagers pour " + trip.getRoute());
        
        StringBuilder sb = new StringBuilder();
        List<Reservation> reservations = trip.getConfirmedReservations();
        
        if (reservations.isEmpty()) {
            sb.append("Aucun passager confirmé pour l'instant.");
        } else {
            for (Reservation r : reservations) {
                sb.append("• ").append(r.getPassenger().getFullName())
                  .append(" (").append(r.getSeatsBooked()).append(" place(s))\n")
                  .append("  📞 ").append(r.getPassenger().getPhoneNumber()).append("\n\n");
            }
        }
        
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
    private void handleCancelTrip(Trip trip) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
            "Êtes-vous sûr de vouloir annuler ce trajet?", 
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Annuler le trajet");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    tripService.cancelTrip(trip);
                    showAlert(Alert.AlertType.INFORMATION, "Annulé", 
                        "Le trajet a été annulé.");
                    showMyTrips();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }
    
    private void showMyVehicles() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Driver)) return;
        
        Driver driver = (Driver) user;
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("🚙 Mes Véhicules");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        Button addBtn = createPrimaryButton("+ Ajouter un véhicule");
        addBtn.setOnAction(e -> showAddVehicleDialog(driver));
        
        VBox vehiclesBox = new VBox(15);
        
        for (Vehicle v : driver.getVehicles()) {
            HBox card = new HBox(15);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
            
            Label icon = new Label("🚗");
            icon.setFont(Font.font("System", 30));
            
            VBox details = new VBox(3);
            HBox.setHgrow(details, Priority.ALWAYS);
            
            Label nameLabel = new Label(v.getFullDescription());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            Label plateLabel = new Label("Immatriculation: " + v.getLicensePlate());
            plateLabel.setStyle("-fx-text-fill: #666;");
            
            details.getChildren().addAll(nameLabel, plateLabel);
            
            Button removeBtn = createDangerButton("Supprimer");
            removeBtn.setOnAction(e -> {
                driver.removeVehicle(v);
                userRepository.save(driver);
                showMyVehicles();
            });
            
            card.getChildren().addAll(icon, details, removeBtn);
            vehiclesBox.getChildren().add(card);
        }
        
        if (vehiclesBox.getChildren().isEmpty()) {
            vehiclesBox.getChildren().add(new Label("Aucun véhicule. Ajoutez-en un pour créer des trajets!"));
        }
        
        content.getChildren().addAll(title, addBtn, vehiclesBox);
        
        mainLayout.setCenter(content);
    }
    
    private void showAddVehicleDialog(Driver driver) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un véhicule");
        dialog.setHeaderText("Entrez les détails du véhicule");
        
        ButtonType addType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField brand = new TextField();
        brand.setPromptText("Marque (ex: Peugeot)");
        
        TextField model = new TextField();
        model.setPromptText("Modèle (ex: 208)");
        
        Spinner<Integer> year = new Spinner<>(2000, 2026, 2022);
        
        TextField plate = new TextField();
        plate.setPromptText("Immatriculation (ex: 123 TUN 4567)");
        
        TextField color = new TextField();
        color.setPromptText("Couleur");
        
        ComboBox<VehicleType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.setValue(VehicleType.SEDAN);
        
        Spinner<Integer> seats = new Spinner<>(1, 8, 4);
        
        grid.add(new Label("Marque:"), 0, 0);
        grid.add(brand, 1, 0);
        grid.add(new Label("Modèle:"), 0, 1);
        grid.add(model, 1, 1);
        grid.add(new Label("Année:"), 0, 2);
        grid.add(year, 1, 2);
        grid.add(new Label("Plaque:"), 0, 3);
        grid.add(plate, 1, 3);
        grid.add(new Label("Couleur:"), 0, 4);
        grid.add(color, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeCombo, 1, 5);
        grid.add(new Label("Places:"), 0, 6);
        grid.add(seats, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addType) {
                Vehicle v = new Vehicle(brand.getText(), model.getText(), year.getValue(),
                    plate.getText(), color.getText(), typeCombo.getValue(), seats.getValue());
                driver.addVehicle(v);
                return v;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(v -> {
            userRepository.save(driver);
            showAlert(Alert.AlertType.INFORMATION, "Véhicule ajouté", 
                "Le véhicule a été ajouté à votre flotte.");
            showMyVehicles();
        });
    }
    
    private void showMyRatings() {
        User user = authService.getCurrentUser();
        if (!(user instanceof Driver)) return;
        
        Driver driver = (Driver) user;
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("⭐ Mes Avis");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Summary
        HBox summaryBox = new HBox(30);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 10;");
        
        VBox avgBox = new VBox(5);
        avgBox.setAlignment(Pos.CENTER);
        Label avgLabel = new Label(driver.getAverageRating() > 0 ? 
            String.format("%.1f", driver.getAverageRating()) : "N/A");
        avgLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        avgLabel.setTextFill(Color.web(PRIMARY_COLOR));
        Label starsLabel = new Label("★★★★★".substring(0, (int) Math.round(driver.getAverageRating())));
        starsLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 24;");
        avgBox.getChildren().addAll(avgLabel, starsLabel, 
            new Label(driver.getRatingCount() + " avis"));
        
        summaryBox.getChildren().add(avgBox);
        
        // Reviews list
        VBox reviewsBox = new VBox(10);
        
        for (Rating r : driver.getRatings()) {
            HBox reviewCard = new HBox(15);
            reviewCard.setPadding(new Insets(15));
            reviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
            
            VBox reviewContent = new VBox(5);
            HBox.setHgrow(reviewContent, Priority.ALWAYS);
            
            Label reviewer = new Label(r.getPassenger().getFirstName() + " - " + r.getFormattedDate());
            reviewer.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            Label stars = new Label(r.getStars());
            stars.setStyle("-fx-text-fill: gold; -fx-font-size: 16;");
            
            Label comment = new Label(r.getComment() != null && !r.getComment().isEmpty() ? 
                "\"" + r.getComment() + "\"" : "Pas de commentaire");
            comment.setStyle("-fx-text-fill: #666;");
            comment.setWrapText(true);
            
            Label trip = new Label("Trajet: " + r.getTripRoute());
            trip.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
            
            reviewContent.getChildren().addAll(reviewer, stars, comment, trip);
            reviewCard.getChildren().add(reviewContent);
            reviewsBox.getChildren().add(reviewCard);
        }
        
        if (reviewsBox.getChildren().isEmpty()) {
            reviewsBox.getChildren().add(new Label("Aucun avis. Complétez des trajets pour recevoir des notes!"));
        }
        
        ScrollPane scrollPane = new ScrollPane(reviewsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        content.getChildren().addAll(title, summaryBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }
    
    // ==================== ADMIN VIEWS ====================
    
    private void showAdminDashboard() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(25));
        
        Label title = new Label("📊 Tableau de Bord Admin");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Stats
        HBox statsBox = new HBox(20);
        
        statsBox.getChildren().addAll(
            createStatCard("Utilisateurs", String.valueOf(userRepository.count()), PRIMARY_COLOR),
            createStatCard("Conducteurs", String.valueOf(userRepository.findAllDrivers().size()), SUCCESS_COLOR),
            createStatCard("Passagers", String.valueOf(userRepository.findAllPassengers().size()), WARNING_COLOR),
            createStatCard("Trajets", String.valueOf(tripRepository.count()), PRIMARY_COLOR),
            createStatCard("Revenus", formatTND(paymentService.getTotalRevenue()), SUCCESS_COLOR)
        );
        
        // Recent activity
        Label activityTitle = new Label("Activité récente");
        activityTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        VBox activityBox = new VBox(10);
        
        // Show last 5 reservations
        List<Reservation> reservations = reservationService.getReservations();
        int count = 0;
        for (int i = reservations.size() - 1; i >= 0 && count < 5; i--, count++) {
            Reservation r = reservations.get(i);
            Label activityLabel = new Label(
                r.getPassenger().getFullName() + " a réservé " + 
                r.getTrip().getRoute() + " - " + r.getStatus().getDisplayName());
            activityLabel.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5;");
            activityBox.getChildren().add(activityLabel);
        }
        
        content.getChildren().addAll(title, statsBox, activityTitle, activityBox);
        
        mainLayout.setCenter(content);
    }
    
    private void showUserManagement() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("👥 Gestion des Utilisateurs");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));

        TabPane userTabs = new TabPane();
        userTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab driversTab = new Tab("Conducteurs");
        driversTab.setContent(createAdminUsersPane(userRepository.findAllDrivers(), true, "Aucun conducteur"));

        Tab passengersTab = new Tab("Passagers");
        passengersTab.setContent(createAdminUsersPane(userRepository.findAllPassengers(), false, "Aucun passager"));

        userTabs.getTabs().addAll(driversTab, passengersTab);

        content.getChildren().addAll(title, userTabs);
        VBox.setVgrow(userTabs, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }
    
    private void showAllTrips() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("🚗 Tous les Trajets");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));

        TabPane tripsTabs = new TabPane();
        tripsTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab openTab = new Tab("Ouverts");
        openTab.setContent(createAdminTripsPane(getAdminTripsByState(false), "Aucun trajet ouvert"));

        Tab completedTab = new Tab("Terminés");
        completedTab.setContent(createAdminTripsPane(getAdminTripsByState(true), "Aucun trajet terminé"));

        tripsTabs.getTabs().addAll(openTab, completedTab);

        content.getChildren().addAll(title, tripsTabs);
        VBox.setVgrow(tripsTabs, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }
    
    private void showAllReservations() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("📋 Toutes les Réservations");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Reservation, String> passengerCol = new TableColumn<>("Passager");
        passengerCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPassenger().getFullName()));
        
        TableColumn<Reservation, String> tripCol = new TableColumn<>("Trajet");
        tripCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTrip().getRoute()));
        
        TableColumn<Reservation, String> seatsCol = new TableColumn<>("Places");
        seatsCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getSeatsBooked())));
        
        TableColumn<Reservation, String> priceCol = new TableColumn<>("Prix (TND)");
        priceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(formatTND(data.getValue().getTotalPrice())));
        
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        
        table.getColumns().addAll(passengerCol, tripCol, seatsCol, priceCol, statusCol);
        table.getItems().addAll(reservationService.getReservations());
        
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(title, table);
        
        mainLayout.setCenter(content);
    }
    
    private void showAllPayments() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        Label title = new Label("💰 Historique des Paiements");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        Label totalLabel = new Label("Revenus totaux: " + formatTND(paymentService.getTotalRevenue()));
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        totalLabel.setTextFill(Color.web(SUCCESS_COLOR));
        
        TableView<Payment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Payment, String> txnCol = new TableColumn<>("Transaction");
        txnCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTransactionId()));
        
        TableColumn<Payment, String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(formatTND(data.getValue().getAmount())));
        
        TableColumn<Payment, String> refundCol = new TableColumn<>("Remboursé");
        refundCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(formatTND(data.getValue().getRefundAmount())));
        
        TableColumn<Payment, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        
        table.getColumns().addAll(txnCol, amountCol, refundCol, statusCol);
        table.getItems().addAll(paymentService.getPayments());
        
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(title, totalLabel, table);
        
        mainLayout.setCenter(content);
    }

    private List<Trip> getAdminTripsByState(boolean completed) {
        List<Trip> result = new java.util.ArrayList<>();
        for (Trip trip : tripService.getTrips()) {
            if (completed) {
                if (trip.getStatus() == TripStatus.COMPLETED) {
                    result.add(trip);
                }
            } else if (trip.getStatus() != TripStatus.COMPLETED && trip.getStatus() != TripStatus.CANCELLED) {
                result.add(trip);
            }
        }
        result.sort((a, b) -> b.getDepartureTime().compareTo(a.getDepartureTime()));
        return result;
    }

    private ScrollPane createAdminTripsPane(List<Trip> trips, String emptyMessage) {
        VBox tripsBox = new VBox(10);

        if (trips.isEmpty()) {
            tripsBox.getChildren().add(new Label(emptyMessage));
        } else {
            for (Trip trip : trips) {
                HBox card = new HBox(15);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");

                VBox details = new VBox(3);
                HBox.setHgrow(details, Priority.ALWAYS);

                details.getChildren().addAll(
                    new Label(trip.getRoute() + " - " + trip.getStatus().getDisplayName()),
                    new Label("Conducteur: " + trip.getDriver().getFullName()),
                    new Label("Places: " + trip.getBookedSeats() + "/" + trip.getTotalSeats())
                );

                card.getChildren().add(details);
                tripsBox.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(tripsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createAdminUsersPane(List<? extends User> users, boolean driverView, String emptyMessage) {
        VBox container = new VBox(10);

        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button suspendBtn = new Button("Suspendre");
            private final Button activateBtn = new Button("Activer");
            private final Button verifyBtn = new Button("Vérifier");

            {
                suspendBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    u.suspendAccount();
                    userRepository.save(u);
                    showUserManagement();
                });

                activateBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    u.activateAccount();
                    userRepository.save(u);
                    showUserManagement();
                });

                verifyBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u instanceof Driver) {
                        ((Driver) u).verify();
                        userRepository.save(u);
                        showUserManagement();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                User u = getTableView().getItems().get(getIndex());
                HBox buttons = new HBox(5);
                buttons.getChildren().add(u.isActive() ? suspendBtn : activateBtn);

                if (driverView && u instanceof Driver && !((Driver) u).isVerified()) {
                    buttons.getChildren().add(verifyBtn);
                }

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(nameCol, emailCol, statusCol, actionsCol);
        table.setItems(FXCollections.observableArrayList(users));

        if (users.isEmpty()) {
            container.getChildren().add(new Label(emptyMessage));
        }
        container.getChildren().add(table);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(table, Priority.ALWAYS);
        return scrollPane;
    }
    
    // ==================== COMMON VIEWS ====================
    
    private void showNotifications() {
        User user = authService.getCurrentUser();
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔔 Notifications");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        Button markAllBtn = createSecondaryButton("Tout marquer lu");
        markAllBtn.setOnAction(e -> {
            notificationService.markAllAsRead(user);
            showNotifications();
        });
        
        header.getChildren().addAll(title, markAllBtn);
        
        VBox notifBox = new VBox(10);
        
        List<Notification> notifications = notificationService.getUserNotifications(user);
        
        if (notifications.isEmpty()) {
            notifBox.getChildren().add(new Label("Aucune notification"));
        } else {
            for (Notification n : notifications) {
                HBox card = new HBox(15);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: " + (n.isRead() ? "white" : "#FFE4E4") + 
                    "; -fx-background-radius: 8; -fx-border-color: #FFE4E4; -fx-border-radius: 8;");
                
                Label icon = new Label(n.isRead() ? "📭" : "📬");
                icon.setFont(Font.font("System", 24));
                
                VBox details = new VBox(3);
                HBox.setHgrow(details, Priority.ALWAYS);
                
                Label titleLabel = new Label(n.getTitle());
                titleLabel.setFont(Font.font("System", n.isRead() ? FontWeight.NORMAL : FontWeight.BOLD, 14));
                
                Label messageLabel = new Label(n.getMessage());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-text-fill: #666;");
                
                Label timeLabel = new Label(n.getTimeSince());
                timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
                
                details.getChildren().addAll(titleLabel, messageLabel, timeLabel);
                
                if (!n.isRead()) {
                    Button readBtn = createSecondaryButton("Marquer lu");
                    readBtn.setOnAction(e -> {
                        notificationService.markAsRead(n);
                        showNotifications();
                    });
                    card.getChildren().addAll(icon, details, readBtn);
                } else {
                    card.getChildren().addAll(icon, details);
                }
                
                notifBox.getChildren().add(card);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(notifBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        content.getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainLayout.setCenter(content);
    }
    
    private void showProfile() {
        User user = authService.getCurrentUser();
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setMaxWidth(500);
        
        Label title = new Label("👤 Mon Profil");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(PRIMARY_COLOR));
        
        VBox profileCard = createStyledCard();
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        
        TextField firstName = new TextField(user.getFirstName());
        styleTextField(firstName);
        
        TextField lastName = new TextField(user.getLastName());
        styleTextField(lastName);
        
        TextField email = new TextField(user.getEmail());
        email.setEditable(false);
        email.setStyle("-fx-background-color: #eee;");
        
        TextField phone = new TextField(user.getPhoneNumber());
        styleTextField(phone);
        
        String roleDisplay = user instanceof Driver ? "Conducteur" : 
                            user instanceof Admin ? "Administrateur" : "Passager";
        
        form.add(new Label("Prénom:"), 0, 0);
        form.add(firstName, 1, 0);
        form.add(new Label("Nom:"), 0, 1);
        form.add(lastName, 1, 1);
        form.add(new Label("Email:"), 0, 2);
        form.add(email, 1, 2);
        form.add(new Label("Téléphone:"), 0, 3);
        form.add(phone, 1, 3);
        form.add(new Label("Rôle:"), 0, 4);
        form.add(new Label(roleDisplay), 1, 4);
        form.add(new Label("Statut:"), 0, 5);
        form.add(new Label(user.getStatus().getDisplayName()), 1, 5);
        
        Button saveBtn = createPrimaryButton("Enregistrer");
        saveBtn.setOnAction(e -> {
            user.updateAccount(firstName.getText(), lastName.getText(), phone.getText());
            userRepository.save(user);
            showAlert(Alert.AlertType.INFORMATION, "Mis à jour", "Profil mis à jour avec succès!");
        });
        
        profileCard.getChildren().addAll(form, saveBtn);
        
        // Change password section
        VBox passwordCard = createStyledCard();
        
        Label pwdTitle = new Label("Changer le mot de passe");
        pwdTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        PasswordField currentPwd = new PasswordField();
        currentPwd.setPromptText("Mot de passe actuel");
        
        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("Nouveau mot de passe");
        
        PasswordField confirmPwd = new PasswordField();
        confirmPwd.setPromptText("Confirmer le nouveau mot de passe");
        
        Button changePwdBtn = createSecondaryButton("Changer");
        changePwdBtn.setOnAction(e -> {
            if (!newPwd.getText().equals(confirmPwd.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas");
                return;
            }
            try {
                authService.changePassword(currentPwd.getText(), newPwd.getText());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe changé!");
                currentPwd.clear();
                newPwd.clear();
                confirmPwd.clear();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });
        
        passwordCard.getChildren().addAll(pwdTitle, currentPwd, newPwd, confirmPwd, changePwdBtn);
        
        content.getChildren().addAll(title, profileCard, passwordCard);
        
        mainLayout.setCenter(content);
    }
    
    private void handleLogout() {
        authService.logout();
        primaryStage.setScene(loginScene);
    }
    
    // ==================== UI HELPERS ====================
    
    private VBox createStyledCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                     "-fx-border-color: #FFE4E4; -fx-border-radius: 10;");
        return card;
    }
    
    private void styleTextField(TextField field) {
        field.setStyle("-fx-padding: 10; -fx-background-radius: 5; " +
                      "-fx-border-color: #ddd; -fx-border-radius: 5;");
    }
    
    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
        return btn;
    }
    
    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #eee; -fx-text-fill: #333; " +
                    "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }
    
    private Button createSuccessButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; " +
                    "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }
    
    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                    "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }
    
    private Button createLinkButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                    "-fx-cursor: hand; -fx-underline: true;");
        return btn;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
