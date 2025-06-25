package com.parkingsystem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Scanner;

public class UserBooking {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void userMenu(Scanner sc) {
        int choice;
        do {
            System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- USER MENU ---" + AnsiColors.RESET);
            System.out.println("1. View Parking Lots & Availability");
            System.out.println("2. Book Parking Slot");
            System.out.println("3. View My Bookings");
            System.out.println("4. Check-In Vehicle");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Extend Booking Time");
            System.out.println("7. Exit Vehicle");
            System.out.println("8. Logout");
            System.out.print(AnsiColors.PURPLE + "Enter your choice: " + AnsiColors.RESET);

            while (!sc.hasNextInt()) {
                System.out.print(AnsiColors.RED + "Enter a valid number: " + AnsiColors.RESET);
                sc.next();
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewLots(sc);
                case 2 -> bookSlot(sc);
                case 3 -> viewMyBooking(sc);
                case 4 -> checkInVehicle(sc);
                case 5 -> cancelMyBooking(sc);
                case 6 -> extendBooking(sc);
                case 7 -> ExitService.processExit(sc);
                case 8 -> System.out.println(AnsiColors.YELLOW + "Returning to main menu..." + AnsiColors.RESET);
                default -> System.out.println(AnsiColors.RED + "Invalid choice. Try again." + AnsiColors.RESET);
            }
        } while (choice != 8);
        UserService.logout();
    }

    private static void viewLots(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- PARKING LOTS AVAILABILITY ---" + AnsiColors.RESET);
        System.out.printf("%-4s | %-8s | %-20s | %-17s | %-10s | %-10s | %-10s | %-10s\n",
                "S.No", "Lot ID", "Name", "Location", "2W Avail", "4W Avail", "2W Price", "4W Price");
        System.out.println("-----------------------------------------------------------------------------------------");
        
        if (DataStore.lots.isEmpty()) {
            System.out.println(AnsiColors.YELLOW + "No parking lots available at the moment." + AnsiColors.RESET);
        } else {
            for (int i = 0; i < DataStore.lots.size(); i++) {
                Lot lot = DataStore.lots.get(i);
                System.out.printf("%-4d | %-8s | %-20s | %-17s | %-10d | %-10d | ₹%-9.2f | ₹%-9.2f\n",
                        (i + 1), lot.lotId, lot.name, lot.location, lot.twoWheel, lot.fourWheel,
                        lot.price2WPerHour, lot.price4WPerHour);
            }
        }
        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void bookSlot(Scanner sc) {
        User user = UserService.getCurrentUser();
        if (user == null) {
            System.out.println(AnsiColors.RED + "Please login to book a slot." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (DataStore.userBookings.containsKey(user.username)) {
            System.out.println(AnsiColors.YELLOW + "You already have an active booking (ID: " + DataStore.userBookings.get(user.username).bookingId + "). Only one active booking per user allowed." + AnsiColors.RESET);
            System.out.println("Please cancel or exit your current booking first.");
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- BOOK A PARKING SLOT ---" + AnsiColors.RESET);

        if (DataStore.lots.isEmpty()) {
            System.out.println(AnsiColors.YELLOW + "No parking lots available to book." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        System.out.println("\nAvailable Parking Lots:");
        for (int i = 0; i < DataStore.lots.size(); i++) {
            Lot lot = DataStore.lots.get(i);
            System.out.println((i + 1) + ". " + lot.name + " (" + lot.lotId + ") - 2W: " + lot.twoWheel + " | 4W: " + lot.fourWheel);
        }
        System.out.print(AnsiColors.PURPLE + "Select a Lot by S.No: " + AnsiColors.RESET);
        int lotChoice;
        try {
            lotChoice = Integer.parseInt(sc.nextLine());
            if (lotChoice <= 0 || lotChoice > DataStore.lots.size()) {
                System.out.println(AnsiColors.RED + "Invalid S.No for lot." + AnsiColors.RESET);
                System.out.println("Press Enter to return to User Menu...");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(AnsiColors.RED + "Invalid input. Please enter a number." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        Lot lot = DataStore.lots.get(lotChoice - 1);

        System.out.println("\nSelect Vehicle Type:");
        System.out.println("1. 2-Wheeler (2W)");
        System.out.println("2. 4-Wheeler (4W)");
        System.out.print(AnsiColors.PURPLE + "Enter your choice: " + AnsiColors.RESET);
        int typeChoice;
        String vehicleType;
        try {
            typeChoice = Integer.parseInt(sc.nextLine());
            if (typeChoice == 1) {
                vehicleType = "2W";
            } else if (typeChoice == 2) {
                vehicleType = "4W";
            } else {
                System.out.println(AnsiColors.RED + "Invalid choice for vehicle type." + AnsiColors.RESET);
                System.out.println("Press Enter to return to User Menu...");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(AnsiColors.RED + "Invalid input. Please enter a number." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (!lot.isAvailable(vehicleType)) {
            System.out.println(AnsiColors.RED + "No slots available for selected type in " + lot.name + ". Please choose another lot or type." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Vehicle Number (e.g., TS09AA1234): ");
        String vehicleNumber = sc.nextLine().trim().toUpperCase();
        if (!vehicleNumber.matches("[A-Z]{2}\\d{2}[A-Z]{2}\\d{4}")) {
            System.out.println(AnsiColors.RED + "Invalid vehicle number format. Use format like TS09AA1234." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (DataStore.vehicleBookings.containsKey(vehicleNumber)) {
            System.out.println(AnsiColors.RED + "Vehicle with number " + vehicleNumber + " already has an active booking (ID: " + DataStore.vehicleBookings.get(vehicleNumber).bookingId + "). Only one active booking per vehicle allowed." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Booking Date (yyyy-MM-dd): ");
        String dateInput = sc.nextLine().trim();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatter.setLenient(false);
        try {
            // Clear time portion to compare dates only
            SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
            Date today = dateOnly.parse(dateOnly.format(new Date()));
            Date inputDateOnly = dateOnly.parse(dateInput);
            if (inputDateOnly.before(today)) {
                System.out.println(AnsiColors.RED + "Cannot book for past dates. Please select today or a future date." + AnsiColors.RESET);
                System.out.println("Press Enter to return to User Menu...");
                sc.nextLine();
                return;
            }
        } catch (ParseException e) {
            System.out.println(AnsiColors.RED + "Invalid date format. Use yyyy-MM-dd (e.g., 2025-06-16)." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Booking Time (HH:mm, 24-hour format): ");
        String timeInput = sc.nextLine().trim();
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        dateTimeFormatter.setLenient(false);
        LocalDateTime bookingDateTime;
        try {
            Date parsedDateTime = dateTimeFormatter.parse(dateInput + "T" + timeInput);
            // Convert to LocalDateTime for storage
            bookingDateTime = LocalDateTime.parse(dateInput + "T" + timeInput);
            SimpleDateFormat timeOnly = new SimpleDateFormat("yyyy-MM-dd");
            Date inputDateOnly = timeOnly.parse(dateInput);
            Date today = timeOnly.parse(timeOnly.format(new Date()));
            if (inputDateOnly.equals(today)) {
                // Check if time is at least 5 minutes in the future
                long diffMillis = parsedDateTime.getTime() - new Date().getTime();
                if (diffMillis < 5 * 60 * 1000) { // Less than 5 minutes
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    System.out.println(AnsiColors.RED + "For today, booking time must be at least 5 minutes after the current time (" + timeFormatter.format(new Date()) + ")." + AnsiColors.RESET);
                    System.out.println("Press Enter to return to User Menu...");
                    sc.nextLine();
                    return;
                }
            }
        } catch (ParseException e) {
            System.out.println(AnsiColors.RED + "Invalid time format. Use HH:mm (e.g., 21:30)." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Duration (in hours): ");
        int durationHours;
        try {
            durationHours = Integer.parseInt(sc.nextLine());
            if (durationHours <= 0) {
                System.out.println(AnsiColors.RED + "Duration must be positive." + AnsiColors.RESET);
                System.out.println("Press Enter to return to User Menu...");
                sc.nextLine();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(AnsiColors.RED + "Invalid duration. Please enter a number." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.YELLOW + "Booking will start at: " + bookingDateTime.format(DATETIME_DISPLAY_FORMATTER) + AnsiColors.RESET);

        double totalAmount = (vehicleType.equals("2W") ? lot.price2WPerHour : lot.price4WPerHour) * durationHours;
        double advancePayment = totalAmount / 2.0;

        System.out.println(AnsiColors.BOLD + AnsiColors.BLUE + "\n--- IMPORTANT BOOKING DETAILS & RULES ---" + AnsiColors.RESET);
        System.out.println("Lot Selected: " + lot.name + " (" + lot.lotId + ")");
        System.out.println("Vehicle Type: " + vehicleType + ", Vehicle Number: " + vehicleNumber);
        System.out.println("Booked Start Time: " + bookingDateTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Booked Duration: " + durationHours + " hours");
        System.out.println("Estimated Total Booking Cost: ₹" + String.format("%.2f", totalAmount));
        System.out.println("Advance Payment Required (50%): ₹" + String.format("%.2f", advancePayment));
        
        System.out.println("\n--- Pricing & Penalty Rules ---");
        System.out.println("- " + vehicleType + " Price: ₹" + String.format("%.2f", (vehicleType.equals("2W") ? lot.price2WPerHour : lot.price4WPerHour)) + "/hour");
        System.out.println("- Cancellation within " + DataStore.GRACE_PERIOD_MINUTES + " minutes of start time: NO FEE");
        System.out.println("- Cancellation after " + DataStore.GRACE_PERIOD_MINUTES + " minutes:");
        System.out.println("  - 2-Wheeler Cancellation Fee: ₹" + String.format("%.2f", lot.cancellationFee2W));
        System.out.println("  - 4-Wheeler Cancellation Fee: ₹" + String.format("%.2f", lot.cancellationFee4W));
        System.out.println("- Overstay Grace Period: " + DataStore.GRACE_PERIOD_MINUTES + " minutes after booked end time.");
        System.out.println("- Overstay Penalty (after grace):");
        System.out.println("  - 2-Wheeler: ₹" + String.format("%.2f", lot.overstayPenalty2WPer30Min) + " per 30 mins");
        System.out.println("  - 4-Wheeler: ₹" + String.format("%.2f", lot.overstayPenalty4WPer30Min) + " per 30 mins");
        System.out.println("Remaining amount + any overstay penalties will be due at exit.");
        System.out.println(AnsiColors.BOLD + AnsiColors.YELLOW + "\nConsider these terms before confirming your booking." + AnsiColors.RESET);
        System.out.print(AnsiColors.PURPLE + "Confirm Booking (Y/N)? " + AnsiColors.RESET);
        String confirm = sc.nextLine();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println(AnsiColors.RED + "Booking cancelled by user." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        String slotNumber;
        if (vehicleType.equals("2W")) {
            slotNumber = "S" + (lot.totalTwoWheelSlots - lot.twoWheel + 1);
        } else {
            slotNumber = "L" + (lot.totalFourWheelSlots - lot.fourWheel + 1);
        }

        Booking booking = new Booking(user.username, lot.lotId, lot.name, lot.location, vehicleType,
                vehicleNumber, durationHours, bookingDateTime, slotNumber);
        booking.amountPaid = advancePayment;

        DataStore.userBookings.put(user.username, booking);
        DataStore.vehicleBookings.put(vehicleNumber, booking);
        DataStore.allBookingsHistory.add(booking);

        lot.allocateSlot(vehicleType);

        System.out.println(AnsiColors.GREEN + "✅ Slot booked successfully!" + AnsiColors.RESET);
        System.out.println(AnsiColors.BOLD + "--- BOOKING DETAILS ---" + AnsiColors.RESET);
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Lot: " + booking.lotName + ", Slot: " + booking.slotNumber);
        System.out.println("Vehicle: " + booking.vehicleType + " - " + booking.vehicleNumber);
        System.out.println("Booked From: " + booking.startTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Booked Until: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Advance Paid: ₹" + String.format("%.2f", booking.amountPaid));
        System.out.println(AnsiColors.YELLOW + "Please check in at the booked time using the Check-In Vehicle option." + AnsiColors.RESET);

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }
   
    private static void viewMyBooking(Scanner sc) {
        User user = UserService.getCurrentUser();
        if (user == null) {
            System.out.println(AnsiColors.RED + "Login required to view your booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking b = DataStore.userBookings.get(user.username);
        if (b == null || b.isCancelled) {
            System.out.println(AnsiColors.YELLOW + "No active booking found for your account." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(b.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Associated lot not found for your booking. Please contact support." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- MY ACTIVE BOOKING ---" + AnsiColors.RESET);
        System.out.printf("%-10s | %-20s | %-12s | %-15s | %-8s | %-6s | %-8s | %-12s\n",
                "Booking ID", "Lot Name", "Date", "Time", "Duration", "Paid", "Status", "Time Info");
        System.out.println("------------------------------------------------------------------------------------------------------");
        
        String timeStatus;
        long elapsedMins = b.getElapsedMinutes();
        long remainingMins = ChronoUnit.MINUTES.between(LocalDateTime.now(), b.endTime);

        if (remainingMins > 0) {
            timeStatus = "Rem: " + remainingMins + "m";
        } else {
            timeStatus = "Elapsed: " + elapsedMins + "m";
        }

        System.out.printf("%-10s | %-20s | %-12s | %-15s | %-8s | ₹%-5.2f | %-8s | %-12s\n",
                b.bookingId, b.lotName, b.startTime.format(DATE_FORMATTER),
                b.startTime.format(TIME_FORMATTER) + "-" + b.endTime.format(TIME_FORMATTER),
                b.durationHours + " hr", b.amountPaid, (b.isCancelled ? "Cancelled" : "Active"), timeStatus);

        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(b.endTime.plusMinutes(DataStore.GRACE_PERIOD_MINUTES))) {
            long minutesOverstayed = ChronoUnit.MINUTES.between(b.endTime.plusMinutes(DataStore.GRACE_PERIOD_MINUTES), currentTime);
            minutesOverstayed = Math.max(0, minutesOverstayed);
            double penalty = b.getOverstayCharge(lot, DataStore.GRACE_PERIOD_MINUTES);
            System.out.println(AnsiColors.RED + "\n⚠️ WARNING: Your booking has overstayed by " + minutesOverstayed + " minutes (after grace period)." + AnsiColors.RESET);
            System.out.println(AnsiColors.RED + "   Applicable penalty: ₹" + String.format("%.2f", penalty) + AnsiColors.RESET);
            if(b.penaltyWaived) {
                System.out.println(AnsiColors.GREEN + "   (Note: Penalty has been waived by admin for this booking)" + AnsiColors.RESET);
            }
            System.out.println(AnsiColors.RED + "   Please proceed to exit or extend your booking immediately." + AnsiColors.RESET);
        } else if (currentTime.isAfter(b.endTime)) {
            long graceRemaining = ChronoUnit.MINUTES.between(currentTime, b.endTime.plusMinutes(DataStore.GRACE_PERIOD_MINUTES));
            graceRemaining = Math.max(0, graceRemaining);
            System.out.println(AnsiColors.YELLOW + "\n⏰ REMINDER: Your booked time for Slot " + b.slotNumber + " ended at " + b.endTime.format(TIME_FORMATTER) + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   You are currently in the " + DataStore.GRACE_PERIOD_MINUTES + "-minute grace period." + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   Grace period remaining: " + graceRemaining + " minutes." + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   Overstay charges will apply after this: " + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   2-Wheeler: ₹" + String.format("%.2f", lot.overstayPenalty2WPer30Min) + " per 30 mins" + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   4-Wheeler: ₹" + String.format("%.2f", lot.overstayPenalty4WPer30Min) + " per 30 mins" + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   Please vacate or extend your booking soon." + AnsiColors.RESET);
        } else if (remainingMins <= 60 && remainingMins > 0) {
            System.out.println(AnsiColors.YELLOW + "\n🔔 REMINDER: Your booking for Slot " + b.slotNumber + " ends in " + remainingMins + " minutes." + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "   Please plan to exit or extend your booking." + AnsiColors.RESET);
        }

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }
    
    private static void checkInVehicle(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to check in a vehicle." + AnsiColors.RESET);
            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
            return;
        }
        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null || booking.isCancelled) {
            System.out.println(AnsiColors.YELLOW + "No active booking found to check in." + AnsiColors.RESET);
            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
            return;
        }
        if (AdminService.checkInVehicle(currentUser.username)) {
            System.out.println(AnsiColors.GREEN + "✅ Check-In successful! Slot start time updated." + AnsiColors.RESET);
        } else {
            System.out.println(AnsiColors.RED + "Check-In failed. Ensure slot is available and booking is valid." + AnsiColors.RESET);
        }
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    private static void cancelMyBooking(Scanner sc) {
        User user = UserService.getCurrentUser();
        if (user == null) {
            System.out.println(AnsiColors.RED + "Login required to cancel your booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking b = DataStore.userBookings.get(user.username);
        if (b == null || b.isCancelled) {
            System.out.println(AnsiColors.YELLOW + "No active booking found to cancel." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- CANCEL BOOKING ---" + AnsiColors.RESET);
        System.out.print("Your current booking ID is " + b.bookingId + ". Enter it to confirm cancellation: ");
        String bookingIdToCancel = sc.nextLine();

        if (!b.bookingId.equalsIgnoreCase(bookingIdToCancel)) {
            System.out.println(AnsiColors.RED + "The entered Booking ID does not match your active booking. Cancellation aborted." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(b.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Associated lot not found for your booking. Cannot process cancellation." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        long timeSinceBookingMinutes = ChronoUnit.MINUTES.between(b.startTime, LocalDateTime.now());
        System.out.println("\nTime since booking started: " + timeSinceBookingMinutes + " minutes.");

        double cancellationFee = 0;
        double refundAmount = b.amountPaid;

        if (timeSinceBookingMinutes > DataStore.GRACE_PERIOD_MINUTES) {
            if (b.vehicleType.equalsIgnoreCase("2W")) {
                cancellationFee = lot.cancellationFee2W;
            } else if (b.vehicleType.equalsIgnoreCase("4W")) {
                cancellationFee = lot.cancellationFee4W;
            }
            refundAmount = b.amountPaid - cancellationFee;
            System.out.println(AnsiColors.YELLOW + "⚠️ Note: Your booking is past the " + DataStore.GRACE_PERIOD_MINUTES + "-minute grace period." + AnsiColors.RESET);
            System.out.println(AnsiColors.YELLOW + "A cancellation fee of ₹" + String.format("%.2f", cancellationFee) + " will be applied." + AnsiColors.RESET);
        } else {
            System.out.println(AnsiColors.GREEN + "No cancellation fee applied. Within " + DataStore.GRACE_PERIOD_MINUTES + "-minute grace period." + AnsiColors.RESET);
        }
        
        System.out.println("Advance Paid: ₹" + String.format("%.2f", b.amountPaid));
        System.out.println("Cancellation Fee: ₹" + String.format("%.2f", cancellationFee));
        System.out.println("Refund Amount: ₹" + String.format("%.2f", refundAmount));

        System.out.print(AnsiColors.PURPLE + "Confirm cancellation (Y/N)? " + AnsiColors.RESET);
        String confirm = sc.nextLine();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println(AnsiColors.RED + "Cancellation aborted by user." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        b.isCancelled = true;
        DataStore.userBookings.remove(user.username);
        DataStore.vehicleBookings.remove(b.vehicleNumber);
        
        lot.releaseSlot(b.vehicleType);

        System.out.println(AnsiColors.GREEN + "✅ Booking " + b.bookingId + " cancelled successfully." + AnsiColors.RESET);
        System.out.println(AnsiColors.GREEN + "Amount refunded: ₹" + String.format("%.2f", Math.max(0, refundAmount)) + AnsiColors.RESET);

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void extendBooking(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to extend your booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null || booking.isCancelled) {
            System.out.println(AnsiColors.YELLOW + "You have no active bookings to extend." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Associated lot not found for your booking. Cannot extend." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- EXTEND BOOKING TIME ---" + AnsiColors.RESET);
        System.out.println("Your Booking ID: " + booking.bookingId);
        System.out.println("Current End Time: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER));

        long remainingMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), booking.endTime);
        if (remainingMinutes < 0) {
            long minutesOverstayed = ChronoUnit.MINUTES.between(booking.endTime, LocalDateTime.now());
            minutesOverstayed = Math.max(0, minutesOverstayed);
            double penalty = booking.getOverstayCharge(lot, DataStore.GRACE_PERIOD_MINUTES);
            System.out.println(AnsiColors.RED + "You have already overstayed by " + minutesOverstayed + " minutes." + AnsiColors.RESET);
            if (penalty > 0) {
                System.out.println(AnsiColors.RED + "Current Overstay Penalty: ₹" + String.format("%.2f", penalty) + AnsiColors.RESET);
            }
            System.out.println(AnsiColors.YELLOW + "Please consider exiting or extending to avoid further charges. Overstay penalties will be added to the new total." + AnsiColors.RESET);
        } else {
            System.out.println("Remaining time: " + remainingMinutes + " minutes.");
        }

        System.out.print("Enter Additional Duration to Extend (in hours): ");
        int extraHours;
        try {
            extraHours = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(AnsiColors.RED + "Invalid input. Please enter a number." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (extraHours <= 0) {
            System.out.println(AnsiColors.YELLOW + "Extension duration must be a positive number of hours." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (booking.vehicleType.equalsIgnoreCase("2W") && lot.twoWheel == 0 && lot.totalTwoWheelSlots > 0) {
            System.out.println(AnsiColors.RED + "Cannot extend booking. All 2-Wheeler slots are currently occupied in " + lot.name + "." + AnsiColors.RESET);
            System.out.println("Please consider exiting the slot if extension is not possible.");
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        } else if (booking.vehicleType.equalsIgnoreCase("4W") && lot.fourWheel == 0 && lot.totalFourWheelSlots > 0) {
            System.out.println(AnsiColors.RED + "Cannot extend booking. All 4-Wheeler slots are currently occupied in " + lot.name + "." + AnsiColors.RESET);
            System.out.println("Please consider exiting the slot if extension is not possible.");
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        
        if (booking.vehicleType.equalsIgnoreCase("2W") && lot.twoWheel == 0 && lot.totalTwoWheelSlots > 0) {
            System.out.println(AnsiColors.RED + "Cannot extend booking. All 2-Wheeler slots are currently occupied in " + lot.name + "." + AnsiColors.RESET);
            return;
        }
        booking.durationHours += extraHours;
        booking.endTime = booking.endTime.plusHours(extraHours);
        double extraAmount = (booking.vehicleType.equalsIgnoreCase("2W") ? lot.price2WPerHour : lot.price4WPerHour) * extraHours;
        
        System.out.println(AnsiColors.GREEN + "✅ Booking " + booking.bookingId + " extended successfully!" + AnsiColors.RESET);
        System.out.println(AnsiColors.GREEN + "New End Time: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER) + AnsiColors.RESET);
        System.out.println(AnsiColors.YELLOW + "An additional charge of ₹" + String.format("%.2f", extraAmount) + " will be added to your total payment at exit." + AnsiColors.RESET);

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }
}