package com.parkingsystem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Booking {
    public String bookingId;
    public String username;
    public String lotId;
    public String lotName;
    public String location;
    public String vehicleType; // "2W" or "4W"
    public String vehicleNumber;
    public int durationHours;
    public double amountPaid;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String slotNumber;
    public boolean isCancelled = false;
    public boolean penaltyWaived = false;

    public Booking(String username, String lotId, String lotName, String location, String vehicleType,
                   String vehicleNumber, int durationHours, LocalDateTime startTime, String slotNumber) {
        this.bookingId = "BK" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        this.username = username;
        this.lotId = lotId;
        this.lotName = lotName;
        this.location = location;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber.toUpperCase();
        this.durationHours = durationHours;
        this.startTime = startTime;
        this.endTime = startTime.plusHours(durationHours);
        this.slotNumber = slotNumber;
        this.amountPaid = 0;
    }

    // Getters for properties, important for AdminService and ExitService to access
    public String getBookingId() { return bookingId; }
    public String getUsername() { return username; }
    public String getLotId() { return lotId; }
    public String getLotName() { return lotName; }
    public String getVehicleType() { return vehicleType; }
    public String getSlotNumber() { return slotNumber; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isPenaltyWaived() { return penaltyWaived; }

    public double getTotalAmount(Lot lot) {
        if (lot == null) return 0;
        if (this.vehicleType.equalsIgnoreCase("2W")) {
            return lot.price2WPerHour * this.durationHours;
        } else if (this.vehicleType.equalsIgnoreCase("4W")) {
            return lot.price4WPerHour * this.durationHours;
        }
        return 0;
    }

    public long getElapsedMinutes() {
        if (startTime.isAfter(LocalDateTime.now())) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
    }

    public long getRemainingMinutes() {
        if (endTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), endTime);
    }

    public boolean isOverstayed(int gracePeriodMinutes) {
        return LocalDateTime.now().isAfter(endTime.plusMinutes(gracePeriodMinutes));
    }

    public double getOverstayCharge(Lot lot, int gracePeriodMinutes) {
        if (penaltyWaived) return 0;
        if (lot == null) return 0;
        if (!isOverstayed(gracePeriodMinutes)) return 0;

        long minutesOverstayed = ChronoUnit.MINUTES.between(endTime.plusMinutes(gracePeriodMinutes), LocalDateTime.now());
        if (minutesOverstayed <= 0) return 0;

        double penaltyRatePer30Min = 0;
        if (this.vehicleType.equalsIgnoreCase("2W")) {
            penaltyRatePer30Min = lot.overstayPenalty2WPer30Min;
        } else if (this.vehicleType.equalsIgnoreCase("4W")) {
            penaltyRatePer30Min = lot.overstayPenalty4WPer30Min;
        }

        long num30MinBlocks = (minutesOverstayed + 29) / 30;
        return num30MinBlocks * penaltyRatePer30Min;
    }
}