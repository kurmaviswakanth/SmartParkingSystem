package com.parkingsystem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class AdminService {
    public static void adminMenu(Scanner sc) {
        int choice;
        do {
            System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- ADMIN MENU ---" + AnsiColors.RESET);
            System.out.println("1. Add New Lot");
            System.out.println("2. Add Slots To Lot");
            System.out.println("3. Remove Slots From Lot");
            System.out.println("4. View Lot Details");
            System.out.println("5. View Total Revenue");
            System.out.println("6. Waive Penalty");
            System.out.println("7. Cancel Booking");
            System.out.println("8. Exit Admin Menu");
            System.out.print(AnsiColors.PURPLE + "Enter your choice: " + AnsiColors.RESET);

            while (!sc.hasNextInt()) {
                System.out.print(AnsiColors.RED + "Enter a valid number: " + AnsiColors.RESET);
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addNewLot(sc);
                case 2 -> addSlots(sc);
                case 3 -> removeSlots(sc);
                case 4 -> viewLotDetails(sc);
                case 5 -> viewTotalRevenue(sc);
                case 6 -> waivePenalty(sc);
                case 7 -> cancelBooking(sc);
                case 8 -> {
                    System.out.print(AnsiColors.PURPLE + "Confirm logout (Y/N)? " + AnsiColors.RESET);
                    if (sc.nextLine().equalsIgnoreCase("Y")) {
                        System.out.println(AnsiColors.GREEN + "Logged out from Admin Menu." + AnsiColors.RESET);
                    } else {
                        choice = 0;
                    }
                }
                default -> System.out.println(AnsiColors.RED + "Invalid choice. Try again." + AnsiColors.RESET);
            }
        } while (choice != 8);
    }

    private static void addNewLot(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- ADD NEW LOT ---" + AnsiColors.RESET);
        System.out.print("Enter Lot ID: ");
        String lotId = sc.nextLine();
        if (DataStore.lots.stream().anyMatch(lot -> lot.lotId.equals(lotId))) {
            System.out.println(AnsiColors.RED + "Lot ID already exists!" + AnsiColors.RESET);
            return;
        }
        System.out.print("Enter Lot Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Location: ");
        String location = sc.nextLine();
        System.out.print("Enter number of 2-wheeler slots: ");
        int twoWheelSlots = sc.nextInt();
        System.out.print("Enter number of 4-wheeler slots: ");
        int fourWheelSlots = sc.nextInt();
        sc.nextLine();
        Lot newLot = new Lot(lotId, name, location, twoWheelSlots, fourWheelSlots);
        System.out.print("Enter 2W Price per Hour: ");
        newLot.price2WPerHour = sc.nextDouble();
        System.out.print("Enter 4W Price per Hour: ");
        newLot.price4WPerHour = sc.nextDouble();
        System.out.print("Enter 2W Cancellation Fee: ");
        newLot.cancellationFee2W = sc.nextDouble();
        System.out.print("Enter 4W Cancellation Fee: ");
        newLot.cancellationFee4W = sc.nextDouble();
        System.out.print("Enter 2W Overstay Penalty per 30 min: ");
        newLot.overstayPenalty2WPer30Min = sc.nextDouble();
        System.out.print("Enter 4W Overstay Penalty per 30 min: ");
        newLot.overstayPenalty4WPer30Min = sc.nextDouble();
        sc.nextLine();
        DataStore.lots.add(newLot);
        System.out.println(AnsiColors.GREEN + "New lot added successfully!" + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }

    private static void addSlots(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- ADD SLOTS TO LOT ---" + AnsiColors.RESET);
        System.out.print("Enter Lot ID: ");
        String lotId = sc.nextLine();
        Lot lot = DataStore.lots.stream().filter(l -> l.lotId.equals(lotId)).findFirst().orElse(null);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Lot not found!" + AnsiColors.RESET);
            return;
        }
        System.out.print("Enter Vehicle Type (2W/4W): ");
        String vehicleType = sc.nextLine();
        System.out.print("Enter number of slots to add: ");
        int count = sc.nextInt();
        sc.nextLine();
        lot.addSlots(vehicleType, count);
        System.out.println(AnsiColors.GREEN + "Slots added successfully!" + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }

    private static void removeSlots(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- REMOVE SLOTS FROM LOT ---" + AnsiColors.RESET);
        System.out.print("Enter Lot ID: ");
        String lotId = sc.nextLine();
        Lot lot = DataStore.lots.stream().filter(l -> l.lotId.equals(lotId)).findFirst().orElse(null);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Lot not found!" + AnsiColors.RESET);
            return;
        }
        System.out.print("Enter Vehicle Type (2W/4W): ");
        String vehicleType = sc.nextLine();
        System.out.print("Enter number of slots to remove: ");
        int count = sc.nextInt();
        sc.nextLine();
        lot.removeSlots(vehicleType, count);
        System.out.println(AnsiColors.GREEN + "Slots removed successfully!" + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }

    private static void viewLotDetails(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- VIEW LOT DETAILS ---" + AnsiColors.RESET);
        System.out.print("Enter Lot ID: ");
        String lotId = sc.nextLine();
        Lot selectedLot = DataStore.getLotById(lotId);
        if (selectedLot == null) {
            System.out.println(AnsiColors.RED + "Lot not found!" + AnsiColors.RESET);
            return;
        }
        System.out.println("Lot ID: " + selectedLot.lotId);
        System.out.println("Name: " + selectedLot.name);
        System.out.println("Location: " + selectedLot.location);
        System.out.println("Total 2W Slots: " + selectedLot.totalTwoWheelSlots);
        System.out.println("Available 2W Slots: " + DataStore.getCurrentAvailable(selectedLot, "2W"));
        System.out.println("Total 4W Slots: " + selectedLot.totalFourWheelSlots);
        System.out.println("Available 4W Slots: " + DataStore.getCurrentAvailable(selectedLot, "4W"));
        System.out.println("2W Price per Hour: ₹" + selectedLot.price2WPerHour);
        System.out.println("4W Price per Hour: ₹" + selectedLot.price4WPerHour);
        System.out.println("2W Cancellation Fee: ₹" + selectedLot.cancellationFee2W);
        System.out.println("4W Cancellation Fee: ₹" + selectedLot.cancellationFee4W);
        System.out.println("2W Overstay Penalty per 30 min: ₹" + selectedLot.overstayPenalty2WPer30Min);
        System.out.println("4W Overstay Penalty per 30 min: ₹" + selectedLot.overstayPenalty4WPer30Min);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }

    private static void viewTotalRevenue(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- TOTAL REVENUE ---" + AnsiColors.RESET);
        double totalRevenue = DataStore.calculateTotalRevenue();
        System.out.printf("Total revenue from all bookings: ₹%.2f%n", totalRevenue);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }

    private static void waivePenalty(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- WAIVE OVERSTAY PENALTY ---" + AnsiColors.RESET);
        System.out.print("Enter Booking ID to waive penalty: ");
        String bookingId = sc.nextLine();
        Booking booking = DataStore.allBookingsHistory.stream()
                .filter(b -> b.bookingId.equals(bookingId))
                .findFirst()
                .orElse(null);
        if (booking == null) {
            System.out.println(AnsiColors.RED + "Booking ID not found!" + AnsiColors.RESET);
            return;
        }
        if (booking.penaltyWaived) {
            System.out.println(AnsiColors.RED + "Penalty already waived for this booking!" + AnsiColors.RESET);
            return;
        }
        booking.penaltyWaived = true;
        System.out.println(AnsiColors.GREEN + "Penalty waived successfully for Booking ID: " + bookingId + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }
    
    public static boolean checkInVehicle(String username) {
        Booking booking = DataStore.userBookings.get(username);
        if (booking == null || booking.isCancelled) {
            System.out.println(AnsiColors.RED + "No active booking found for this user." + AnsiColors.RESET);
            return false;
        }
        Lot lot = DataStore.lots.stream()
            .filter(l -> l.lotId.equals(booking.lotId))
            .findFirst()
            .orElse(null);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Parking lot not found for this booking." + AnsiColors.RESET);
            return false;
        }
        long minutesLate = ChronoUnit.MINUTES.between(booking.startTime, LocalDateTime.now());
        if (minutesLate > 15) {
            System.out.println(AnsiColors.RED + "Check-in too late. Please extend or rebook your slot." + AnsiColors.RESET);
            return false;
        }
        booking.checkedIn = true;
        booking.arrivalTime = LocalDateTime.now();
        return true;
    }
    
    private static void cancelBooking(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- CANCEL USER BOOKING ---" + AnsiColors.RESET);
        System.out.print("Enter Booking ID: ");
        String bookingId = sc.nextLine();
        Booking booking = DataStore.getBookingById(bookingId);
        if (booking == null || booking.isCancelled) {
            System.out.println(AnsiColors.RED + "Booking not found or already cancelled." + AnsiColors.RESET);
            return;
        }
        Lot lot = DataStore.getLotById(booking.lotId);
        if (lot == null) {
            System.out.println(AnsiColors.RED + "Associated lot not found." + AnsiColors.RESET);
            return;
        }
        booking.isCancelled = true;
        DataStore.userBookings.remove(booking.username);
        DataStore.vehicleBookings.remove(booking.vehicleNumber);
        System.out.println(AnsiColors.GREEN + "Booking " + bookingId + " cancelled successfully." + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to Admin Menu...");
        sc.nextLine();
    }
}