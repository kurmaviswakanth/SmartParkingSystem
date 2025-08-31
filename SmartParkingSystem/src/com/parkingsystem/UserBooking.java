package com.parkingsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class UserBooking {

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
                        (i + 1), lot.lotId, lot.name, lot.location, DataStore.getCurrentAvailable(lot, "2W"), DataStore.getCurrentAvailable(lot, "4W"),
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
        viewLots(sc);  // Show lots first for reference
        System.out.print("Enter Lot ID to book: ");
        String lotId = sc.nextLine().trim();
        Lot selectedLot = DataStore.getLotById(lotId);
        if (selectedLot == null) {
            System.out.println(AnsiColors.RED + "Invalid Lot ID." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Vehicle Type (2W/4W): ");
        String vehicleType = sc.nextLine().trim().toUpperCase();
        if (!vehicleType.equals("2W") && !vehicleType.equals("4W")) {
            System.out.println(AnsiColors.RED + "Invalid vehicle type." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Enter Vehicle Number (e.g., TS07AB1234): ");
        String vehicleNumber = sc.nextLine().trim().toUpperCase();
        if (!vehicleNumber.matches("^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$")) {
            System.out.println(AnsiColors.RED + "Invalid vehicle number format (e.g., TS07AB1234)." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        if (DataStore.vehicleBookings.containsKey(vehicleNumber)) {
            System.out.println(AnsiColors.RED + "This vehicle already has an active booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Duration (in hours, min 1): ");
        int durationHours;
        try {
            durationHours = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(AnsiColors.RED + "Invalid input. Please enter a number." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        if (durationHours < 1) {
            System.out.println(AnsiColors.RED + "Duration must be at least 1 hour." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Start Date (yyyy-MM-dd): ");
        String date = sc.nextLine();
        System.out.print("Enter Start Time (HH:mm): ");
        String time = sc.nextLine();
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(date + " " + time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            System.out.println(AnsiColors.RED + "Invalid date or time format." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            System.out.println(AnsiColors.RED + "Start time cannot be in the past." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        LocalDateTime endTime = startTime.plusHours(durationHours);

        if (!DataStore.isSlotAvailable(lotId, vehicleType, startTime, endTime, null)) {
            System.out.println(AnsiColors.RED + "No slot available for the selected time period in " + selectedLot.name + "." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.print("Enter Slot Number (optional, press Enter to skip): ");
        String slotNumber = sc.nextLine().trim();

        Booking newBooking = new Booking(user.username, selectedLot.lotId, selectedLot.name, selectedLot.location, vehicleType,
                vehicleNumber, durationHours, startTime, slotNumber.isEmpty() ? "Auto" : slotNumber);

        double totalAmount = newBooking.getTotalAmount(selectedLot);
        double advancePayment = totalAmount / 2;
        newBooking.amountPaid = advancePayment;

        DataStore.userBookings.put(user.username, newBooking);
        DataStore.vehicleBookings.put(vehicleNumber, newBooking);
        DataStore.allBookingsHistory.add(newBooking);

        System.out.println(AnsiColors.GREEN + "✅ Booking successful! Booking ID: " + newBooking.bookingId + AnsiColors.RESET);
        System.out.println("Lot: " + newBooking.lotName + ", Location: " + newBooking.location);
        System.out.println("Vehicle: " + vehicleType + " - " + vehicleNumber);
        System.out.println("From: " + startTime.format(DATETIME_DISPLAY_FORMATTER) + " to " + endTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Total Amount: ₹" + String.format("%.2f", totalAmount));
        System.out.println(AnsiColors.YELLOW + "Advance Payment (50%): ₹" + String.format("%.2f", advancePayment) + ". Please pay at the counter." + AnsiColors.RESET);

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void viewMyBooking(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to view your booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null) {
            System.out.println(AnsiColors.YELLOW + "You have no active bookings." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Parking lot not found for your booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- MY ACTIVE BOOKING ---" + AnsiColors.RESET);
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Lot: " + booking.lotName + ", Location: " + booking.location);
        System.out.println("Vehicle Type: " + booking.vehicleType + ", Vehicle No: " + booking.vehicleNumber);
        System.out.println("Booked From: " + booking.startTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Booked Until: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER));
        System.out.println("Duration: " + booking.durationHours + " hours");
        System.out.println("Advance Paid: ₹" + String.format("%.2f", booking.amountPaid));
        System.out.println("Total Booking Amount: ₹" + String.format("%.2f", booking.getTotalAmount(lot)));

        if (booking.completed) {
            System.out.println(AnsiColors.GREEN + "Status: Completed" + AnsiColors.RESET);
            System.out.println("Exit Time: " + booking.exitTime.format(DATETIME_DISPLAY_FORMATTER));
        } else if (booking.isCancelled) {
            System.out.println(AnsiColors.RED + "Status: Cancelled" + AnsiColors.RESET);
        } else {
            if (booking.checkedIn) {
                System.out.println("Arrival Time: " + booking.arrivalTime.format(DATETIME_DISPLAY_FORMATTER));
                System.out.println("Parked Time: " + booking.getParkedMinutes() + " mins");
            } else {
                System.out.println(AnsiColors.YELLOW + "Status: Pending Check-In" + AnsiColors.RESET);
                long minutesToStart = ChronoUnit.MINUTES.between(LocalDateTime.now(), booking.startTime);
                if (minutesToStart > 0) {
                    System.out.println("Starts in: " + minutesToStart + " mins");
                } else {
                    long minutesLate = -minutesToStart;
                    System.out.println(AnsiColors.RED + "You are " + minutesLate + " mins late. Check-in soon to avoid auto-cancellation." + AnsiColors.RESET);
                }
            }
            long remainingMinutes = booking.getRemainingMinutes();
            System.out.println("Remaining Time: " + remainingMinutes + " mins");
            if (booking.isOverstayed(DataStore.GRACE_PERIOD_MINUTES)) {
                double overstay = booking.getOverstayCharge(lot, DataStore.GRACE_PERIOD_MINUTES);
                System.out.println(AnsiColors.RED + "Overstayed! Current Penalty: ₹" + String.format("%.2f", overstay) + AnsiColors.RESET);
            }
        }

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void checkInVehicle(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to check-in." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null || booking.isCancelled) {
            System.out.println(AnsiColors.RED + "No active booking found." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (booking.checkedIn) {
            System.out.println(AnsiColors.YELLOW + "Vehicle already checked in." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Parking lot not found for this booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        long minutesLate = ChronoUnit.MINUTES.between(booking.startTime, LocalDateTime.now());
        if (minutesLate > 15) {
            System.out.println(AnsiColors.RED + "Check-in too late. Booking cancelled automatically." + AnsiColors.RESET);
            booking.isCancelled = true;
            DataStore.userBookings.remove(currentUser.username);
            DataStore.vehicleBookings.remove(booking.vehicleNumber.toUpperCase());
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        booking.checkedIn = true;
        booking.arrivalTime = LocalDateTime.now();

        System.out.println(AnsiColors.GREEN + "✅ Vehicle checked in successfully at " + booking.arrivalTime.format(DATETIME_DISPLAY_FORMATTER) + "!" + AnsiColors.RESET);
        if (minutesLate < 0) {
            System.out.println("You checked in early by " + Math.abs(minutesLate) + " mins.");
        } else if (minutesLate > 0) {
            System.out.println("You checked in late by " + minutesLate + " mins.");
        }
        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void cancelMyBooking(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to cancel a booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null) {
            System.out.println(AnsiColors.YELLOW + "You have no active bookings to cancel." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        if (booking.completed) {
            System.out.println(AnsiColors.RED + "Cannot cancel a completed booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Error: Parking lot not found for your booking. Cannot cancel." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- CANCEL BOOKING ---" + AnsiColors.RESET);
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Lot: " + booking.lotName + ", Vehicle: " + booking.vehicleType + " - " + booking.vehicleNumber);
        System.out.println("From: " + booking.startTime.format(DATETIME_DISPLAY_FORMATTER) + " to " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER));

        System.out.print("Confirm cancellation (Y/N)? ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println(AnsiColors.YELLOW + "Cancellation aborted." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        double cancellationFee = booking.vehicleType.equalsIgnoreCase("2W") ? lot.cancellationFee2W : lot.cancellationFee4W;
        double refund = booking.amountPaid - cancellationFee;

        booking.isCancelled = true;
        DataStore.userBookings.remove(currentUser.username);
        DataStore.vehicleBookings.remove(booking.vehicleNumber.toUpperCase());

        System.out.println(AnsiColors.GREEN + "✅ Booking " + booking.bookingId + " cancelled successfully!" + AnsiColors.RESET);
        System.out.println("Cancellation Fee: ₹" + String.format("%.2f", cancellationFee));
        if (refund > 0) {
            System.out.println(AnsiColors.GREEN + "Refund Amount: ₹" + String.format("%.2f", refund) + ". Collect at the counter." + AnsiColors.RESET);
        } else if (refund < 0) {
            System.out.println(AnsiColors.RED + "Additional Cancellation Fee Due: ₹" + String.format("%.2f", Math.abs(refund)) + ". Please pay at the counter." + AnsiColors.RESET);
        } else {
            System.out.println(AnsiColors.YELLOW + "No refund or additional fee." + AnsiColors.RESET);
        }

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }

    private static void extendBooking(Scanner sc) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            System.out.println(AnsiColors.RED + "Please login to extend a booking." + AnsiColors.RESET);
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }

        Booking booking = DataStore.userBookings.get(currentUser.username);
        if (booking == null || booking.isCancelled || booking.completed) {
            System.out.println(AnsiColors.RED + "No active booking found to extend." + AnsiColors.RESET);
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

        LocalDateTime newEnd = booking.endTime.plusHours(extraHours);
        if (!DataStore.isSlotAvailable(booking.lotId, booking.vehicleType, booking.startTime, newEnd, booking.bookingId)) {
            System.out.println(AnsiColors.RED + "Cannot extend booking. Slot not available for the additional time in " + lot.name + "." + AnsiColors.RESET);
            System.out.println("Please consider exiting if extension is not possible.");
            System.out.println("Press Enter to return to User Menu...");
            sc.nextLine();
            return;
        }
        
        booking.durationHours += extraHours;
        booking.endTime = newEnd;
        double extraAmount = (booking.vehicleType.equalsIgnoreCase("2W") ? lot.price2WPerHour : lot.price4WPerHour) * extraHours;
        
        System.out.println(AnsiColors.GREEN + "✅ Booking " + booking.bookingId + " extended successfully!" + AnsiColors.RESET);
        System.out.println(AnsiColors.GREEN + "New End Time: " + booking.endTime.format(DATETIME_DISPLAY_FORMATTER) + AnsiColors.RESET);
        System.out.println(AnsiColors.YELLOW + "An additional charge of ₹" + String.format("%.2f", extraAmount) + " will be added to your total payment at exit." + AnsiColors.RESET);

        System.out.println("\nPress Enter to return to User Menu...");
        sc.nextLine();
    }
}