package com.carpool.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a payment method (credit card) for a passenger.
 */
public class PaymentMethod {
    
    private final String id;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private boolean isDefault;
    private Passenger owner;
    private String cardType;
    
    /**
     * Creates a new PaymentMethod.
     */
    public PaymentMethod(String cardNumber, String cardHolderName, 
                         String expiryDate, String cvv) {
        this(UUID.randomUUID().toString(), cardNumber, cardHolderName, expiryDate, cvv, false);
    }

    /**
     * Creates a payment method with a fixed identity and default flag.
     */
    public PaymentMethod(String id, String cardNumber, String cardHolderName,
                         String expiryDate, String cvv, boolean isDefault) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.cardNumber = Objects.requireNonNull(cardNumber, "Card number cannot be null");
        this.cardHolderName = Objects.requireNonNull(cardHolderName, "Card holder name cannot be null");
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date cannot be null");
        this.cvv = cvv;
        this.isDefault = isDefault;
        this.cardType = detectCardType(cardNumber);
    }
    
    // ==================== Card Operations ====================
    
    /**
     * Returns the masked card number (showing last 4 digits).
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
    
    /**
     * Validates the payment method.
     */
    public boolean isValid() {
        // Check card number length
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 13) {
            return false;
        }
        
        // Check expiry date
        if (!isExpiryValid()) {
            return false;
        }
        
        // Check CVV
        if (cvv == null || cvv.length() < 3) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the card has not expired.
     */
    public boolean isExpiryValid() {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) {
                return false;
            }
            
            int month = Integer.parseInt(parts[0].trim());
            int year = Integer.parseInt(parts[1].trim());
            
            // Assume 2-digit year
            if (year < 100) {
                year += 2000;
            }
            
            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            return expiry.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Detects card type based on card number prefix.
     */
    private String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }
        
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        
        if (cleanNumber.startsWith("4")) {
            return "Visa";
        } else if (cleanNumber.startsWith("5") || cleanNumber.startsWith("2")) {
            return "Mastercard";
        } else if (cleanNumber.startsWith("3")) {
            return "Amex";
        } else {
            return "Other";
        }
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public Passenger getOwner() {
        return owner;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    /**
     * Returns a display string for the payment method.
     */
    public String getDisplayString() {
        return cardType + " " + getMaskedCardNumber() + 
               (isDefault ? " (Default)" : "");
    }
    
    // ==================== Setters ====================
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        this.cardType = detectCardType(cardNumber);
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public void setOwner(Passenger owner) {
        this.owner = owner;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id='" + id + '\'' +
                ", card=" + getMaskedCardNumber() +
                ", holder='" + cardHolderName + '\'' +
                ", type='" + cardType + '\'' +
                ", default=" + isDefault +
                '}';
    }
}
