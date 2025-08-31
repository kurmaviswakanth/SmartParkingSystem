package com.parkingsystem;

import java.util.Scanner;

public class Rules {

    public static void displayRulesAndRegulations(Scanner sc) {
        System.out.println(AnsiColors.BOLD + AnsiColors.CYAN + "\n--- SMART PARKING: RULES AND REGULATIONS ---" + AnsiColors.RESET);

        // --- Booking & Payment Rules ---
        System.out.println(AnsiColors.YELLOW + "\n[Booking & Payment]" + AnsiColors.RESET);
        System.out.println("1. One Booking Limit: One active booking per user and per vehicle at a time.");
        System.out.println("2. 50% Advance Payment: Confirm your booking with a 50% advance payment.");
        System.out.println("3. Final Payment at Exit: Pay the remaining balance and any extra charges upon exit.");
        System.out.println("4. Vehicle Registration: Each booking requires a unique vehicle number.");

        // --- Duration, Overstay & Extension Rules ---
        System.out.println(AnsiColors.YELLOW + "\n[Time & Overstay]" + AnsiColors.RESET);
        System.out.println("5. Overstay Grace Period: You have a " + DataStore.GRACE_PERIOD_MINUTES + "-minute grace period after your booking ends to exit without penalty.");
        System.out.println("6. Overstay Penalty: A fee is charged for every 30 minutes you overstay past the grace period.");
        System.out.println("7. Extend Booking: You can extend your time only if a similar slot is available.");

        // --- Cancellation & Other Policies ---
        System.out.println(AnsiColors.YELLOW + "\n[Cancellations & Policies]" + AnsiColors.RESET);
        System.out.println("8. Cancellation Fee: A fee will be applied if you cancel your booking.");
        System.out.println("9. No-Show Policy: Your slot is released if you don't check in within 15 minutes of your booking start time.");
        
        // --- Admin Authority ---
        System.out.println(AnsiColors.YELLOW + "\n[Admin Authority]" + AnsiColors.RESET);
        System.out.println("10. Admin Rights: Admins can manage lots, view revenue, and waive penalties.");
        
        System.out.println();
        System.out.println(AnsiColors.RED + AnsiColors.UNDERLINE + "Please adhere to these rules for a smooth parking experience." + AnsiColors.RESET);

        System.out.print("\nPress Enter to return to the previous menu...");
        sc.nextLine();
    }

}
