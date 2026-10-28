package com.carpool.service;

import com.carpool.exception.*;
import com.carpool.model.*;
import com.carpool.repository.*;
import java.util.List;

/**
 * Service for reservation management.
 * Handles booking, confirmation, and cancellation of reservations.
 */
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    public ReservationService(ReservationRepository reservationRepository,
                              PaymentService paymentService,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }
    
    /**
     * Creates a new reservation with payment authorization.
     */
    public Reservation createReservation(Passenger passenger, Trip trip, int seats, 
                                          PaymentMethod paymentMethod) 
            throws ReservationException, TripException, PaymentException {
        
        // Validate inputs
        if (passenger == null) {
            throw new ReservationException("Passenger is required");
        }
        
        if (trip == null) {
            throw TripException.notFound();
        }
        
        if (paymentMethod == null) {
            throw PaymentException.invalidPaymentMethod();
        }
        
        // Check trip availability
        if (!trip.isOpen()) {
            throw TripException.tripNotOpen();
        }
        
        // Check seats
        if (seats <= 0) {
            throw ReservationException.invalidSeats();
        }
        
        if (seats > trip.getAvailableSeats()) {
            throw TripException.noSeatsAvailable();
        }
        
        // Check for duplicate reservation
        if (reservationRepository.existsByPassengerAndTrip(passenger, trip)) {
            throw ReservationException.duplicateReservation();
        }
        
        // Create reservation
        Reservation reservation = new Reservation(passenger, trip, seats);
        reservation.createReservation();
        
        // Authorize payment (hold funds)
        try {
            Payment payment = paymentService.authorizePayment(reservation, paymentMethod);
            reservation.setPayment(payment);
        } catch (PaymentException e) {
            throw e;
        }
        
        // Add to trip and passenger
        if (!trip.addPassenger(reservation)) {
            throw new ReservationException("Failed to add reservation to trip");
        }
        passenger.addReservation(reservation);
        
        // Save
        reservationRepository.save(reservation);
        
        // Notify passenger
        notificationService.sendNotification(passenger, 
            "Reservation Pending", 
            "Your reservation for " + trip.getRoute() + " is pending confirmation. " +
            "Payment of " + reservation.getTotalPrice() + "€ has been authorized.",
            NotificationType.EMAIL);
        
        // Notify driver
        notificationService.sendEmail(trip.getDriver(), 
            "New Reservation Request", 
            passenger.getFullName() + " wants to book " + seats + " seat(s) on your trip " +
            "from " + trip.getDepartureCity() + " to " + trip.getArrivalCity() + ".");
        
        return reservation;
    }
    
    /**
     * Confirms a pending reservation (captures payment).
     */
    public boolean confirmReservation(Reservation reservation) 
            throws ReservationException, PaymentException {
        
        if (reservation == null) {
            throw ReservationException.notFound();
        }
        
        if (reservation.isConfirmed()) {
            throw ReservationException.alreadyConfirmed();
        }
        
        if (reservation.isCancelled()) {
            throw ReservationException.alreadyCancelled();
        }
        
        if (reservation.getPayment() == null) {
            throw ReservationException.paymentRequired();
        }
        
        // Capture payment
        boolean captured = paymentService.capturePayment(reservation.getPayment());
        
        if (captured) {
            reservation.confirmReservation();
            reservationRepository.save(reservation);
            
            // Notify passenger
            notificationService.sendNotification(reservation.getPassenger(), 
                "Reservation Confirmed!", 
                "Your reservation for " + reservation.getTrip().getRoute() + 
                " has been confirmed. " + reservation.getTotalPrice() + "€ has been charged.",
                NotificationType.BOTH);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Cancels a reservation with appropriate refund.
     */
    public boolean cancelReservation(Reservation reservation) 
            throws ReservationException, PaymentException {
        
        if (reservation == null) {
            throw ReservationException.notFound();
        }
        
        if (reservation.isCancelled()) {
            throw ReservationException.alreadyCancelled();
        }

        if (reservation.isCompleted() || reservation.getTrip().isCompleted()) {
            throw ReservationException.cannotCancel();
        }
        
        Trip trip = reservation.getTrip();
        double refundAmount = reservation.calculateRefundAmount();
        
        // Process refund
        if (reservation.getPayment() != null) {
            if (reservation.getPayment().isCaptured()) {
                paymentService.refund(reservation.getPayment(), refundAmount);
            } else if (reservation.getPayment().isAuthorized()) {
                // Just release the authorization
                reservation.getPayment().fullRefund();
            }
        }
        
        // Cancel reservation
        reservation.cancelReservation();
        
        // Remove from trip
        trip.removePassenger(reservation);
        
        // Save
        reservationRepository.save(reservation);
        
        // Notify passenger
        String message = "Your reservation for " + trip.getRoute() + " has been cancelled.";
        if (refundAmount > 0) {
            message += " You will receive a refund of " + refundAmount + "€.";
        }
        
        notificationService.sendNotification(reservation.getPassenger(), 
            "Reservation Cancelled", message, NotificationType.EMAIL);
        
        // Notify driver
        notificationService.sendEmail(trip.getDriver(), 
            "Reservation Cancelled", 
            reservation.getPassenger().getFullName() + " has cancelled their reservation " +
            "for your trip from " + trip.getDepartureCity() + " to " + trip.getArrivalCity() + ". " +
            trip.getAvailableSeats() + " seat(s) now available.");
        
        return true;
    }
    
    /**
     * Refunds a reservation (manual refund).
     */
    public double refundReservation(Reservation reservation) 
            throws ReservationException, PaymentException {
        
        if (reservation == null) {
            throw ReservationException.notFound();
        }
        
        if (reservation.getPayment() == null) {
            throw new ReservationException("No payment found for this reservation");
        }
        
        double refundAmount = reservation.calculateRefundAmount();
        
        if (reservation.getPayment().isCaptured()) {
            paymentService.refund(reservation.getPayment(), refundAmount);
        }
        
        reservation.refundReservation(refundAmount);
        reservationRepository.save(reservation);
        
        return refundAmount;
    }
    
    /**
     * Gets all reservations.
     */
    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }
    
    /**
     * Gets reservation by ID.
     */
    public Reservation getReservationById(String id) throws ReservationException {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw ReservationException.notFound();
        }
        return reservation;
    }
    
    /**
     * Gets reservations for a passenger.
     */
    public List<Reservation> getPassengerReservations(Passenger passenger) {
        return reservationRepository.findByPassenger(passenger);
    }
    
    /**
     * Gets reservations for a trip.
     */
    public List<Reservation> getTripReservations(Trip trip) {
        return reservationRepository.findByTrip(trip);
    }
    
    /**
     * Gets pending reservations for a trip (for driver to confirm).
     */
    public List<Reservation> getPendingReservations(Trip trip) {
        List<Reservation> tripReservations = reservationRepository.findByTrip(trip);
        tripReservations.removeIf(r -> r.getStatus() != ReservationStatus.PENDING);
        return tripReservations;
    }
}
