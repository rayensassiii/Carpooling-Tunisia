# 🚗 Covoiturage Tunisie - تونس

Une plateforme de covoiturage complète pour la Tunisie, développée avec Java et JavaFX, respectant une architecture propre, les principes OOP et SOLID.

A complete carpooling platform for Tunisia built with Java and JavaFX, featuring clean architecture, OOP principles, and SOLID design.

## 📋 Table des matières / Table of Contents

1. [Aperçu du projet](#aperçu-du-projet)
2. [Architecture](#architecture)
3. [Fonctionnalités](#fonctionnalités)
4. [Règles métier](#règles-métier)
5. [Structure du projet](#structure-du-projet)
6. [Comment exécuter](#comment-exécuter)
7. [Comptes de démonstration](#comptes-de-démonstration)
8. [Diagramme UML](#diagramme-uml)

---

## 🎯 Aperçu du projet

Covoiturage Tunisie est une plateforme complète avec trois rôles d'utilisateurs:

- **Passager**: Rechercher des trajets, réserver des places, gérer les réservations, noter les conducteurs
- **Conducteur**: Créer des trajets, gérer les véhicules, accepter les réservations, gagner de l'argent
- **Admin**: Gérer les utilisateurs, vérifier les conducteurs, voir les statistiques

### 🇹🇳 Localisation Tunisienne

- **Villes**: Tunis, Sousse, Sfax, Monastir, Gabès, Bizerte, Kairouan, Hammamet, Nabeul, Djerba...
- **Devise**: Dinar Tunisien (TND)
- **Format de date**: dd/MM/yyyy
- **Téléphone**: +216...
- **Thème**: Rouge et blanc (couleurs du drapeau tunisien)
- **Langue**: Français / Arabe

### Technologies utilisées

- Java 17+
- JavaFX (GUI)
- OOP + Principes SOLID
- Architecture propre (Model/Service/Repository)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   COUCHE PRÉSENTATION                        │
│                   (JavaFX - CarpoolApp)                     │
├─────────────────────────────────────────────────────────────┤
│                    COUCHE SERVICES                           │
│  AuthService │ TripService │ ReservationService │ ...       │
├─────────────────────────────────────────────────────────────┤
│                   COUCHE REPOSITORY                          │
│  UserRepository │ TripRepository │ ReservationRepository    │
├─────────────────────────────────────────────────────────────┤
│                     MODÈLE DOMAINE                           │
│  User │ Trip │ Reservation │ Payment │ Vehicle │ Rating     │
└─────────────────────────────────────────────────────────────┘
```

### Principes de conception appliqués

1. **Single Responsibility Principle (SRP)**: Chaque classe a une seule responsabilité
2. **Open/Closed Principle**: Entités ouvertes à l'extension, fermées à la modification
3. **Liskov Substitution**: Les sous-classes User (Passenger, Driver, Admin) sont interchangeables
4. **Interface Segregation**: Interfaces Notifiable, Authenticable, Refundable
5. **Dependency Inversion**: Les services dépendent d'abstractions (repositories)

---

## ✨ Fonctionnalités

### 👤 Gestion des utilisateurs
- [x] Inscription (Passager/Conducteur)
- [x] Connexion/Déconnexion avec gestion de session
- [x] Blocage de compte après 3 tentatives échouées
- [x] Suspension de compte par l'admin
- [x] Gestion du profil

### 🚗 Gestion des trajets
- [x] Créer des trajets avec sélection de véhicule
- [x] Rechercher des trajets par ville et date
- [x] Filtrer les trajets par prix et places
- [x] Gestion du statut (Ouvert → Complet → En cours → Terminé)
- [x] Auto-COMPLET quand la dernière place est réservée

### 📋 Système de réservation
- [x] Réserver des places sur les trajets
- [x] Autorisation de paiement à la réservation
- [x] Capture du paiement à la confirmation
- [x] Annulation avec politique de remboursement:
  - `> 24h`: Remboursement total
  - `< 24h`: Remboursement 50%
- [x] Annulation conducteur avec compensation (20%)

### 💳 Système de paiement
- [x] Plusieurs moyens de paiement par utilisateur
- [x] Flux Autoriser → Capturer
- [x] Remboursements complets et partiels
- [x] Historique des paiements (en TND)

### 🔔 Notifications
- [x] Notifications email (simulées)
- [x] Notifications SMS (simulées)
- [x] Confirmations de réservation
- [x] Alertes d'annulation
- [x] Demandes de notation

### ⭐ Système de notation
- [x] Noter les conducteurs après trajets terminés
- [x] Notes de 1 à 5 étoiles avec commentaires
- [x] Calcul de la note moyenne
- [x] Affichage de la note sur les cartes de trajets

---

## 📜 Règles métier

| Règle | Implémentation |
|-------|----------------|
| Le trajet devient COMPLET automatiquement | Quand availableSeats atteint 0 |
| Le conducteur ne peut annuler que s'il n'y a pas de réservations | canCancel() vérifie hasActiveReservations() |
| Annulation conducteur < 24h → compensation | 20% par place aux passagers |
| Annulation passager > 24h → remboursement total | calculateRefundAmount() retourne 100% |
| Annulation passager < 24h → remboursement partiel | calculateRefundAmount() retourne 50% |
| Blocage après tentatives échouées | 3 tentatives puis blockUser() |
| Paiement autorisé immédiatement | À createReservation() |
| Paiement capturé quand le conducteur accepte | À confirmReservation() |

---

## 🚀 Comment exécuter

### Prérequis

1. **Java JDK 17 ou supérieur** - [Télécharger](https://adoptium.net/)
2. **JavaFX SDK 17+** - [Télécharger](https://openjfx.io/)

### Option 1: Ligne de commande

```bash
# 1. Naviguer vers le répertoire du projet
cd "C:\Users\user\OneDrive - Ministere de l'Enseignement Superieur et de la Recherche Scientifique\Bureau\projet poo\carpool2\src\com\carpool"

# 2. Compiler tous les fichiers Java
javac --module-path "CHEMIN_VERS_JAVAFX\lib" --add-modules javafx.controls *.java

# 3. Exécuter l'application
java --module-path "CHEMIN_VERS_JAVAFX\lib" --add-modules javafx.controls CarpoolApp
```

### Option 2: IDE (IntelliJ IDEA / Eclipse)

1. Créer un nouveau projet Java
2. Ajouter la bibliothèque JavaFX au projet
3. Copier tous les fichiers .java dans le dossier src
4. Configurer les options VM:
   ```
   --module-path "CHEMIN_VERS_JAVAFX\lib" --add-modules javafx.controls
   ```
5. Exécuter `CarpoolApp.java`

---

## 👥 Comptes de démonstration

L'application est livrée avec des données de test tunisiennes:

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| **Passager** | youssef.hammami@email.tn | pass123 |
| **Passager** | sarra.mejri@email.tn | pass123 |
| **Passager** | khaled.mansour@email.tn | pass123 |
| **Conducteur** | ahmed.benali@email.tn | pass123 |
| **Conducteur** | fatma.trabelsi@email.tn | pass123 |
| **Conducteur** | mohamed.gharbi@email.tn | pass123 |
| **Admin** | admin@covoiturage.tn | admin123 |

### Exemples de trajets

| Trajet | Date | Prix |
|--------|------|------|
| Tunis → Sousse | Demain 07:00 | 15.000 TND |
| Sousse → Sfax | Dans 2 jours | 18.000 TND |
| Tunis → Hammamet | Demain 09:00 | 10.000 TND |
| Monastir → Kairouan | Dans 3 jours | 12.000 TND |
| Tunis → Bizerte | Demain 06:30 | 8.000 TND |
| Sfax → Gabès | Dans 2 jours | 20.000 TND |
| Nabeul → Tunis | Dans 4 jours | 8.500 TND |
| Djerba → Médenine | Dans 5 jours | 7.000 TND |

---

## 🇹🇳 Thème Tunisien

### Couleurs
- **Rouge primaire**: #E70013 (Rouge du drapeau tunisien)
- **Rouge secondaire**: #C41E3A 
- **Fond**: #FFF5F5 (Rose clair)
- **Cartes**: Blanc avec bordure rose

### Villes disponibles
Tunis, Sousse, Sfax, Monastir, Gabès, Bizerte, Kairouan, Ariana, Gafsa, La Marsa, Hammamet, Nabeul, Ben Arous, Kasserine, Médenine, Tozeur, Mahdia, Béja, Jendouba, Sidi Bouzid, Siliana, Zaghouan, Kébili, Tataouine, Djerba, El Jem, Tabarka, Douz, Zarzis, Carthage

---

## 📊 Diagramme UML

Voir `CovoiturageApp_uml.puml` pour le diagramme de classes PlantUML complet.

Pour visualiser:
1. Installer l'extension PlantUML dans VS Code
2. Ouvrir le fichier .puml
3. Prévisualiser le diagramme

Ou utiliser en ligne: [Serveur Web PlantUML](http://www.plantuml.com/plantuml/)

---

## 🔒 Fonctionnalités de sécurité

- **Encapsulation**: Tous les champs sont privés avec getters/setters
- **Copie défensive**: Collections retournées comme nouvelles copies ArrayList
- **Protection des mots de passe**: Mots de passe stockés dans des champs protégés
- **Verrouillage de compte**: Après 3 tentatives échouées
- **Validation des entrées**: Toutes les méthodes de service valident les entrées
- **Exceptions personnalisées**: Exceptions spécifiques pour chaque type d'erreur

---

## 📄 Licence

Ce projet est à des fins éducatives.

---

## 👨‍💻 Auteur

Construit comme une démonstration complète de plateforme de covoiturage avec architecture propre et meilleures pratiques.

🇹🇳 **Vive la Tunisie!** 🇹🇳
