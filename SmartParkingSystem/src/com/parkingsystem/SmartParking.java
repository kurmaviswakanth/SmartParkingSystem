package com.parkingsystem;

import java.util.Scanner;

public class SmartParking {

    public static void main(String[] args) {
    	DataStore.startReminderThread();
        Scanner sc = new Scanner(System.in);
        int choice;

        // Initialize sample data
        Lot lot1 = new Lot("L001", "Hitech Parking", "Hitech City", 50, 20);
        lot1.price2WPerHour = 30;  // IT
        lot1.price4WPerHour = 60;
        lot1.cancellationFee2W = 20;
        lot1.cancellationFee4W = 30;
        lot1.overstayPenalty2WPer30Min = 40;
        lot1.overstayPenalty4WPer30Min = 60;
        DataStore.lots.add(lot1);

        Lot lot2 = new Lot("L002", "RGIA Airport", "Shamshabad", 60, 25);
        lot2.price2WPerHour = 50;  // Airport
        lot2.price4WPerHour = 80;
        lot2.cancellationFee2W = lot1.cancellationFee2W;
        lot2.cancellationFee4W = lot1.cancellationFee4W;
        lot2.overstayPenalty2WPer30Min = lot1.overstayPenalty2WPer30Min;
        lot2.overstayPenalty4WPer30Min = lot1.overstayPenalty4WPer30Min;
        DataStore.lots.add(lot2);

        Lot lot3 = new Lot("L003", "Hyderabad Deccan", "Nampally", 45, 18);
        lot3.price2WPerHour = 35;  // Rail station
        lot3.price4WPerHour = 55;
        lot3.cancellationFee2W = lot1.cancellationFee2W;
        lot3.cancellationFee4W = lot1.cancellationFee4W;
        lot3.overstayPenalty2WPer30Min = lot1.overstayPenalty2WPer30Min;
        lot3.overstayPenalty4WPer30Min = lot1.overstayPenalty4WPer30Min;
        DataStore.lots.add(lot3);

        Lot lot4 = new Lot("L004", "Gachibowli Hub", "Gachibowli", 50, 22);
        lot4.price2WPerHour = 28;  // IT + Residential
        lot4.price4WPerHour = 50;
        lot4.cancellationFee2W = lot1.cancellationFee2W;
        lot4.cancellationFee4W = lot1.cancellationFee4W;
        lot4.overstayPenalty2WPer30Min = lot1.overstayPenalty2WPer30Min;
        lot4.overstayPenalty4WPer30Min = lot1.overstayPenalty4WPer30Min;
        DataStore.lots.add(lot4);

        Lot lot5 = new Lot("L005", "Secunderabad Station", "Secunderabad", 55, 20);
        lot5.price2WPerHour = 32;  // rail Station
        lot5.price4WPerHour = 52;
        lot5.cancellationFee2W = lot1.cancellationFee2W;
        lot5.cancellationFee4W = lot1.cancellationFee4W;
        lot5.overstayPenalty2WPer30Min = lot1.overstayPenalty2WPer30Min;
        lot5.overstayPenalty4WPer30Min = lot1.overstayPenalty4WPer30Min;
        DataStore.lots.add(lot5);

        do {
            System.out.println(AnsiColors.BOLD + AnsiColors.BLUE + "\n============================================" + AnsiColors.RESET);
            System.out.println(AnsiColors.BOLD + AnsiColors.BLUE + "      SMART PARKING SLOT BOOKING SYSTEM" + AnsiColors.RESET);
            System.out.println(AnsiColors.BOLD + AnsiColors.BLUE + "============================================" + AnsiColors.RESET);
            System.out.println("1. User Login");
            System.out.println("2. User Registration");
            System.out.println("3. Admin Login");
            System.out.println("4. Rules and Regulations");
            System.out.println("5. Exit");
            System.out.print(AnsiColors.PURPLE + "Enter your choice: " + AnsiColors.RESET);

            while (!sc.hasNextInt()) {
                System.out.print(AnsiColors.RED + "Enter a valid number: " + AnsiColors.RESET);
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> {
                    if (UserService.login(sc)) {
                        UserBooking.userMenu(sc);
                    }
                }
                case 2 -> UserService.register(sc);
                case 3 -> {
                    if (UserService.adminLogin(sc)) {
                        System.out.println(AnsiColors.YELLOW + "Entering Admin Menu..." + AnsiColors.RESET);
                        AdminService.adminMenu(sc);
                    }
                }
                case 4 -> Rules.displayRulesAndRegulations(sc);
                case 5 -> System.out.println(AnsiColors.YELLOW + "Thank you for using Smart Parking System!" + AnsiColors.RESET);
                default -> System.out.println(AnsiColors.RED + "Invalid choice. Try again." + AnsiColors.RESET);
            }
        } while (choice != 5);

        sc.close();
    }

    public static Lot createLot(String id, String name, String location, int avail2W, int avail4W, Lot config) {
        Lot lot = new Lot(id, name, location, avail2W, avail4W);
        lot.cancellationFee2W = config.cancellationFee2W;
        lot.cancellationFee4W = config.cancellationFee4W;
        lot.overstayPenalty2WPer30Min = config.overstayPenalty2WPer30Min;
        lot.overstayPenalty4WPer30Min = config.overstayPenalty4WPer30Min;
        return lot;
    }
}
