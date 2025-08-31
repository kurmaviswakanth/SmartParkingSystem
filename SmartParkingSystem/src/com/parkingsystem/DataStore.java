package com.parkingsystem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        for(Booking b : userBookings.values()) {
            if(!b.isCancelled) {
                active.add(b);
            }
        }
        return active;
    }

    public static List<Booking> getOverstayedBookings() {
        List<Booking> overstayed = new ArrayList<>();
        for (Booking b : userBookings.values()) {
            if (b.isCancelled) continue;
            
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
                    double cancellationFee = b.vehicleType.equalsIgnoreCase("2W") 
                        ? lot.cancellationFee2W 
                        : lot.cancellationFee4W;
                    totalRevenue += cancellationFee;
                } else {
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
        for (Booking b : allBookingsHistory) {
            if (b.bookingId.equalsIgnoreCase(bookingId)) {
                return b;
            }
        }
        return null;
    }

    public static int getCurrentAvailable(Lot lot, String vehicleType) {
        int occupied = (int) allBookingsHistory.stream()
                .filter(b -> b.lotId.equals(lot.lotId) && b.vehicleType.equalsIgnoreCase(vehicleType)
                        && b.checkedIn && !b.completed && !b.isCancelled)
                .count();
        return vehicleType.equalsIgnoreCase("2W") ? lot.totalTwoWheelSlots - occupied : lot.totalFourWheelSlots - occupied;
    }

    static class Event implements Comparable<Event> {
        LocalDateTime time;
        int type; // 1 for start, -1 for end

        Event(LocalDateTime time, int type) {
            this.time = time;
            this.type = type;
        }

        @Override
        public int compareTo(Event o) {
            int cmp = this.time.compareTo(o.time);
            if (cmp == 0) {
                return Integer.compare(this.type, o.type); // ends (-1) before starts (1)
            }
            return cmp;
        }
    }

    public static boolean isSlotAvailable(String lotId, String vehicleType, LocalDateTime start, LocalDateTime end, String excludeBookingId) {
        Lot lot = getLotById(lotId);
        if (lot == null) return false;

        List<Booking> relevant = allBookingsHistory.stream()
                .filter(b -> !b.isCancelled && b.lotId.equals(lotId) && b.vehicleType.equalsIgnoreCase(vehicleType)
                        && b.endTime.isAfter(start) && b.startTime.isBefore(end)
                        && (excludeBookingId == null || !b.bookingId.equals(excludeBookingId)))
                .toList();

        if (relevant.isEmpty()) return true;

        List<Event> events = new ArrayList<>();
        for (Booking b : relevant) {
            events.add(new Event(b.startTime, 1));
            events.add(new Event(b.endTime, -1));
        }

        Collections.sort(events);

        int current = 0;
        int max = 0;
        for (Event e : events) {
            current += e.type;
            max = Math.max(current, max);
        }

        int capacity = vehicleType.equalsIgnoreCase("2W") ? lot.totalTwoWheelSlots : lot.totalFourWheelSlots;
        return max < capacity;
    }
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startReminderThread() {
        scheduler.scheduleAtFixedRate(() -> {
            List<Booking> bookingsCopy = new ArrayList<>(userBookings.values());
            for (Booking b : bookingsCopy) {
                if (b.isCancelled) continue;

                // Auto-cancel no-show
                if (!b.checkedIn && LocalDateTime.now().isAfter(b.startTime.plusMinutes(15))) {
                    b.isCancelled = true;
                    userBookings.remove(b.username);
                    vehicleBookings.remove(b.vehicleNumber);
                    
                    synchronized (System.out) {
                        System.out.println(AnsiColors.YELLOW + "🔔 Auto-cancelled no-show booking: " + b.bookingId + AnsiColors.RESET);
                    }
                }

                // Reminder for expiration
                long remainingMins = ChronoUnit.MINUTES.between(LocalDateTime.now(), b.endTime);
                if (remainingMins > 10 && remainingMins <= 15) {
                    System.out.println(AnsiColors.YELLOW + "🔔 Reminder: Booking " + b.bookingId + " for " + b.vehicleNumber +
                            " at " + b.lotName + " expires in " + remainingMins + " minutes!" + AnsiColors.RESET);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
}