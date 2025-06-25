package com.parkingsystem;

import java.util.Random;
import java.util.Scanner;

public class OtpService {
    private static String generatedOtp = null;

    // Generate a 6-digit OTP
    public static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 100000 to 999999
        generatedOtp = String.valueOf(otp);
        return generatedOtp;
    }

    // Verify OTP and reset password
    public static boolean verifyOtpAndResetPassword(Scanner sc, String username, boolean isAdmin) {
        System.out.print("Enter OTP: ");
        String enteredOtp = sc.nextLine().trim();
        if (!enteredOtp.equals(generatedOtp)) {
            System.out.println(AnsiColors.RED + "Invalid OTP. Password reset failed." + AnsiColors.RESET);
            return false;
        }

        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine().trim();
        if (newPassword.isEmpty()) {
            System.out.println(AnsiColors.RED + "Password cannot be empty." + AnsiColors.RESET);
            return false;
        }

        if (isAdmin) {
            UserService.updateAdminPassword(newPassword);
            System.out.println(AnsiColors.GREEN + "✅ Admin password reset successfully!" + AnsiColors.RESET);
        } else {
            UserService.updateUserPassword(username, newPassword);
            System.out.println(AnsiColors.GREEN + "✅ Password reset successfully for user " + username + "!" + AnsiColors.RESET);
        }
        generatedOtp = null; // Clear OTP after use
        return true;
    }
}