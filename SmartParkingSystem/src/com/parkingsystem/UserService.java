package com.parkingsystem;

import java.util.Scanner;

public class UserService {
    private static User currentUser = null;
    private static boolean isAdminLoggedIn = false;
    private static String adminUsername = "admin";
    private static String adminPassword = "admin123";
    private static String adminPhoneNumber = "7989393735";

    public static void register(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- USER REGISTRATION ---" + AnsiColors.RESET);
        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();
        if (username.equalsIgnoreCase(adminUsername) || DataStore.users.stream().anyMatch(u -> u.username.equalsIgnoreCase(username))) {
            System.out.println(AnsiColors.RED + "Username already exists. Try another." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();
        System.out.print("Enter Phone Number (10 digits): ");
        String phoneNumber = sc.nextLine().trim();
        if (phoneNumber.length() != 10 || !isAllDigits(phoneNumber)) {
            System.out.println(AnsiColors.RED + "Invalid phone number. Must be 10 digits." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        if (phoneNumber.equals(adminPhoneNumber) || DataStore.users.stream().anyMatch(u -> u.phoneNumber.equals(phoneNumber))) {
            System.out.println(AnsiColors.RED + "Phone number already in use. Try another." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        User newUser = new User(username, password, phoneNumber);
        DataStore.users.add(newUser);
        System.out.println(AnsiColors.GREEN + "✅ Registration successful! Please login to continue." + AnsiColors.RESET);
        System.out.println("\nPress Enter to return to main menu...");
        sc.nextLine();
    }

    private static boolean isAllDigits(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean login(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- USER LOGIN ---" + AnsiColors.RESET);
        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        for (User user : DataStore.users) {
            if (user.username.equalsIgnoreCase(username) && user.password.equals(password)) {
                currentUser = user;
                System.out.println(AnsiColors.GREEN + "✅ Login successful! Welcome, " + username + "." + AnsiColors.RESET);
                return true;
            }
        }

        System.out.println(AnsiColors.RED + "Invalid username or password." + AnsiColors.RESET);
        System.out.print(AnsiColors.PURPLE + "Forgot Password? (Y/N): " + AnsiColors.RESET);
        String forgot = sc.nextLine().trim();
        if (forgot.equalsIgnoreCase("Y")) {
            handleForgotUserPassword(sc, username);
        } else {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        }
        return false;
    }

    public static boolean adminLogin(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- ADMIN LOGIN ---" + AnsiColors.RESET);
        System.out.print("Enter Admin Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine().trim();

        if (username.equals(adminUsername) && password.equals(adminPassword)) {
            isAdminLoggedIn = true;
            System.out.println(AnsiColors.GREEN + "✅ Admin login successful!" + AnsiColors.RESET);
            return true;
        }

        System.out.println(AnsiColors.RED + "Invalid admin credentials." + AnsiColors.RESET);
        System.out.print(AnsiColors.PURPLE + "Forgot Password? (Y/N): " + AnsiColors.RESET);
        String forgot = sc.nextLine().trim();
        if (forgot.equalsIgnoreCase("Y")) {
            handleForgotAdminPassword(sc, username);
        } else {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
        isAdminLoggedIn = false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdminLoggedIn() {
        return isAdminLoggedIn;
    }

    private static void handleForgotUserPassword(Scanner sc, String username) {
        User user = DataStore.users.stream()
                .filter(u -> u.username.equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
        if (user == null) {
            System.out.println(AnsiColors.RED + "Username not found." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        System.out.print("Enter registered phone number (10 digits): ");
        String phoneNumber = sc.nextLine().trim();
        if (!phoneNumber.equals(user.phoneNumber)) {
            System.out.println(AnsiColors.RED + "Invalid phone number for this user." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        String otp = OtpService.generateOtp();
        System.out.println(AnsiColors.YELLOW + "Your OTP is: " + otp + " (Enter this to reset password)" + AnsiColors.RESET);
        if (OtpService.verifyOtpAndResetPassword(sc, username, false)) {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        } else {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        }
    }

    private static void handleForgotAdminPassword(Scanner sc, String username) {
        if (!username.equals(adminUsername)) {
            System.out.println(AnsiColors.RED + "Invalid admin username." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        System.out.print("Enter admin phone number (10 digits): ");
        String phoneNumber = sc.nextLine().trim();
        if (!phoneNumber.equals(adminPhoneNumber)) {
            System.out.println(AnsiColors.RED + "Invalid phone number for admin." + AnsiColors.RESET);
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
            return;
        }
        String otp = OtpService.generateOtp();
        System.out.println(AnsiColors.YELLOW + "Your OTP is: " + otp + " (Enter this to reset password)" + AnsiColors.RESET);
        if (OtpService.verifyOtpAndResetPassword(sc, username, true)) {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        } else {
            System.out.println("\nPress Enter to return to main menu...");
            sc.nextLine();
        }
    }

    public static void updateUserPassword(String username, String newPassword) {
        for (User user : DataStore.users) {
            if (user.username.equalsIgnoreCase(username)) {
                user.password = newPassword;
                break;
            }
        }
    }

    public static void updateAdminPassword(String newPassword) {
        adminPassword = newPassword;
    }
}