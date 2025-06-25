package com.parkingsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DataStore {

    public static List<Lot> lots = new ArrayList<>();
    public static List<User> users = new ArrayList<>();
    // userBookings maps username to their current active booking
    public static HashMap<String, Booking> userBookings = new HashMap<>();
    // vehicleBookings maps vehicleNumber to their current active booking
    public static HashMap<String, Booking> vehicleBookings = new HashMap<>();
    // allBookingsHistory stores all bookings, active, cancelled or completed.
    public static List<Booking> allBookingsHistory = new ArrayList<>();


    public static final int GRACE_PERIOD_MINUTES = 10;

    public static List<Booking> getAllActiveBookings() {
        List<Booking> active = new ArrayList<>();
        // Iterate userBookings.values() as these are currently active bookings.
        for(Booking b : userBookings.values()) {
            if(!b.isCancelled) { // Double check, though userBookings should only hold active ones
                active.add(b);
            }
        }
        return active;
    }

    public static List<Booking> getOverstayedBookings() {
        List<Booking> overstayed = new ArrayList<>();
        // Iterate all active bookings to find overstayed ones
        for (Booking b : userBookings.values()) { // Only check active bookings for overstay
            if (b.isCancelled) continue; // Skip cancelled bookings
            
            Lot lot = getLotById(b.lotId);
            if (lot != null && b.isOverstayed(GRACE_PERIOD_MINUTES)) {
                overstayed.add(b);
            }
        }
        return overstayed;
    }

    public static double calculateTotalRevenue() {
        double totalRevenue = 0;
        for (Booking b : allBookingsHistory) {
            Lot lot = getLotById(b.lotId);
            if (lot != null) {
                if (b.isCancelled) {
                    // Compute cancellation fee based on time since booking and vehicle type
                    long timeSinceBookingMinutes = ChronoUnit.MINUTES.between(b.startTime, LocalDateTime.now());
                    double cancellationFee = 0;
                    if (timeSinceBookingMinutes > GRACE_PERIOD_MINUTES) {
                        cancellationFee = b.vehicleType.equalsIgnoreCase("2W") 
                            ? lot.cancellationFee2W 
                            : lot.cancellationFee4W;
                    }
                    // Only add the lesser of the amount paid or applicable cancellation fee
                    totalRevenue += Math.min(b.amountPaid, cancellationFee);
                } else {
                    // For active/completed bookings, sum base amount + overstay penalties if any
                    totalRevenue += b.getTotalAmount(lot);
                    if (b.isOverstayed(GRACE_PERIOD_MINUTES) && !b.penaltyWaived) {
                        totalRevenue += b.getOverstayCharge(lot, GRACE_PERIOD_MINUTES);
                    }
                }
            }
        }
        return totalRevenue;
    }


    public static Lot getLotById(String lotId) {
        for (Lot lot : lots) {
            if (lot.lotId.equalsIgnoreCase(lotId)) {
                return lot;
            }
        }
        return null;
    }
    
    public static Lot getLotByName(String lotName) {
        for (Lot lot : lots) {
            if (lot.name.equalsIgnoreCase(lotName)) {
                return lot;
            }
        }
        return null;
    }

    public static Booking getBookingById(String bookingId) {
        // Search in allBookingsHistory, as admin might look up old/cancelled bookings
        for (Booking b : allBookingsHistory) {
            if (b.bookingId.equalsIgnoreCase(bookingId)) {
                return b;
            }
        }
        return null;
    }
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startReminderThread() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Booking b : userBookings.values()) {
                if (b.isCancelled) continue;
                long remainingMins = ChronoUnit.MINUTES.between(LocalDateTime.now(), b.endTime);
                if (remainingMins > 10 && remainingMins <= 15) {
                    System.out.println(AnsiColors.YELLOW + "🔔 Reminder: Booking " + b.bookingId + " for " + b.vehicleNumber +
                        " at " + b.lotName + " expires in " + remainingMins + " minutes!" + AnsiColors.RESET);
                }
            }
        }, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
    }
}