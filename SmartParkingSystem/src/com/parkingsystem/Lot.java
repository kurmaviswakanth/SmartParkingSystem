package com.parkingsystem;

public class Lot {
    public String lotId;
    public String name;
    public String location;

    public int totalTwoWheelSlots;     // Total 2-wheeler capacity
    public int totalFourWheelSlots;    // Total 4-wheeler capacity

    public double price2WPerHour;
    public double price4WPerHour;

    public double cancellationFee2W;
    public double cancellationFee4W;

    public double overstayPenalty2WPer30Min;
    public double overstayPenalty4WPer30Min;

    // --- Full Constructor (used in initial data setup) ---
    public Lot(String lotId, String name, String location,
               int totalTwoWheelSlots, int totalFourWheelSlots,
               double price2WPerHour, double price4WPerHour,
               double cancellationFee2W, double cancellationFee4W,
               double overstayPenalty2WPer30Min, double overstayPenalty4WPer30Min) {

        this.lotId = lotId;
        this.name = name;
        this.location = location;

        this.totalTwoWheelSlots = totalTwoWheelSlots;
        this.totalFourWheelSlots = totalFourWheelSlots;

        this.price2WPerHour = price2WPerHour;
        this.price4WPerHour = price4WPerHour;

        this.cancellationFee2W = cancellationFee2W;
        this.cancellationFee4W = cancellationFee4W;

        this.overstayPenalty2WPer30Min = overstayPenalty2WPer30Min;
        this.overstayPenalty4WPer30Min = overstayPenalty4WPer30Min;
    }

    // --- Short Constructor (minimal fields, rest default to 0) ---
    public Lot(String lotId, String name, String location, int totalTwoWheelSlots, int totalFourWheelSlots) {
        this(lotId, name, location, totalTwoWheelSlots, totalFourWheelSlots,
             0.0, 0.0,   // price per hour
             0.0, 0.0,   // cancellation fee
             0.0, 0.0);  // overstay penalty
    }

    // --- Admin: Add Slots ---
    public void addSlots(String vehicleType, int count) {
        if (count <= 0) return;
        if (vehicleType.equalsIgnoreCase("2W")) {
            totalTwoWheelSlots += count;
        } else if (vehicleType.equalsIgnoreCase("4W")) {
            totalFourWheelSlots += count;
        }
    }

    // --- Admin: Remove Slots ---
    public void removeSlots(String vehicleType, int count) {
        if (count <= 0) return;
        int avail = DataStore.getCurrentAvailable(this, vehicleType);
        int actualRemove = Math.min(count, avail);

        if (vehicleType.equalsIgnoreCase("2W")) {
            totalTwoWheelSlots -= actualRemove;
            if (totalTwoWheelSlots < 0) totalTwoWheelSlots = 0;
        } else if (vehicleType.equalsIgnoreCase("4W")) {
            totalFourWheelSlots -= actualRemove;
            if (totalFourWheelSlots < 0) totalFourWheelSlots = 0;
        }
    }
}