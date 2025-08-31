package com.parkingsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class ExitService {

    private static final DateTimeFormatter DATETIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void processExit(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login first to exit a vehicle." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null) {
            System.out.println(AnsiColors.YELLOW + "You have no active bookings to exit." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (booking.isCancelled) {
            System.out.println(AnsiColors.YELLOW + "This booking has been cancelled. No exit processing needed." + AnsiColors.RESET);
            DataStore.userBookings.remove(currentUser.username);
            DataStore.vehicleBookings.remove(booking.vehicleNumber.toUpperCase());
            System.out.println("\nPress Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (!booking.checkedIn) {
            System.out.println(AnsiColors.RED + "Please check-in your vehicle first before exiting." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Parking lot not found for this booking. Cannot process exit." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long parkedMinutes = booking.getParkedMinutes();
        long bookedMinutes = (long)booking.durationHours * 60;

        double totalOriginalBookingCost = booking.getTotalAmount(lot);
        
        double overstayCharge = booking.getOverstayCharge(lot, DataStore.GRACE_PERIOD_MINUTES);

        double finalPaymentDue = (totalOriginalBookingCost + overstayCharge) - booking.amountPaid;
        
        DataStore.userBookings.remove(currentUser.username);
        DataStore.vehicleBookings.remove(booking.vehicleNumber.toUpperCase());
        
        booking.exitTime = now;
        booking.completed = true;

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- VEHICLE EXIT RECEIPT ---" + AnsiColors.RESET);
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Lot: " + booking.lotName + ", Location: " + booking.location);
        System.out.println("Vehicle Type: " + booking.vehicleType + ", Vehicle No: " + booking.vehicleNumber);
        System.out.println("Booked From: " + booking.startTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Booked Until: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER));
        if (booking.arrivalTime != null) {
            System.out.println("Arrival Time: " + booking.arrivalTime.format(DATETIME_DISPLAY_FORMATTER));
        }
        System.out.println("Actual Exit Time: " + now.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Duration Booked: " + booking.durationHours + " hours (" + bookedMinutes + " mins)");
        System.out.println("Actual Parked Time: " + parkedMinutes + " mins");
        System.out.println("Advance Paid: ₹" + String.format("%.2f", booking.amountPaid));
        System.out.println("Calculated Original Booking Value: ₹" + String.format("%.2f", totalOriginalBookingCost));

        if (overstayCharge > 0) {
            long minutesOverstayedActual = ChronoUnit.MINUTES.between(booking.endTime.plusMinutes(DataStore.GRACE_PERIOD_MINUTES), now);
            minutesOverstayedActual = Math.max(0, minutesOverstayedActual);
            System.out.println(AnsiColors.RED + "Overstayed by " + minutesOverstayedActual + " mins (after grace period)." + AnsiColors.RESET);
            System.out.println(AnsiColors.RED + "Overstay Penalty: ₹" + String.format("%.2f", overstayCharge) + AnsiColors.RESET);
            if (booking.penaltyWaived) {
                System.out.println(AnsiColors.GREEN + "(Penalty was waived by Admin)" + AnsiColors.RESET);
            }
        } else {
            System.out.println("No overstay penalty applied. Thank you!");
        }

        System.out.println(AnsiColors.GREEN + "Total Due at Exit: ₹" + String.format("%.2f", finalPaymentDue) + AnsiColors.RESET);
        
        if (finalPaymentDue > 0.01) {
            System.out.println(AnsiColors.YELLOW + "\nPlease pay the remaining amount at the counter." + AnsiColors.RESET);
        } else if (finalPaymentDue < -0.01) {
            System.out.println(AnsiColors.YELLOW + "\nRefund Due: ₹" + String.format("%.2f", Math.abs(finalPaymentDue)) + ". Please collect your refund at the counter." + AnsiColors.RESET);
        } else {
            System.out.println(AnsiColors.GREEN + "\nPayment settled. Thank you for using our service!" + AnsiColors.RESET);
        }

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }
}