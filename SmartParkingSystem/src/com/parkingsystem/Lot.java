package com.parkingsystem;

public class Lot {
    public String lotId;
    public String name;
    public String location;

    public int twoWheel;       // Available 2-wheeler slots
    public int fourWheel;      // Available 4-wheeler slots
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
               int twoWheel, int fourWheel,
               double price2WPerHour, double price4WPerHour,
               double cancellationFee2W, double cancellationFee4W,
               double overstayPenalty2WPer30Min, double overstayPenalty4WPer30Min) {

        this.lotId = lotId;
        this.name = name;
        this.location = location;

        this.twoWheel = twoWheel;
        this.fourWheel = fourWheel;

        this.totalTwoWheelSlots = twoWheel;
        this.totalFourWheelSlots = fourWheel;

        this.price2WPerHour = price2WPerHour;
        this.price4WPerHour = price4WPerHour;

        this.cancellationFee2W = cancellationFee2W;
        this.cancellationFee4W = cancellationFee4W;

        this.overstayPenalty2WPer30Min = overstayPenalty2WPer30Min;
        this.overstayPenalty4WPer30Min = overstayPenalty4WPer30Min;
    }

    // --- Short Constructor (minimal fields, rest default to 0) ---
    public Lot(String lotId, String name, String location, int twoWheel, int fourWheel) {
        this(lotId, name, location, twoWheel, fourWheel,
             0.0, 0.0,   // price per hour
             0.0, 0.0,   // cancellation fee
             0.0, 0.0);  // overstay penalty
    }

    // --- Check Availability ---
    public boolean isAvailable(String vehicleType) {
        return vehicleType.equalsIgnoreCase("2W") ? twoWheel > 0 :
               vehicleType.equalsIgnoreCase("4W") ? fourWheel > 0 : false;
    }

    // --- Allocate a Slot ---
    public synchronized void allocateSlot(String vehicleType) {
        if (vehicleType.equalsIgnoreCase("2W")) {
            if (twoWheel > 0) {
                twoWheel--;
            } else {
                throw new IllegalStateException("No available 2W slots.");
            }
        } else if (vehicleType.equalsIgnoreCase("4W")) {
            if (fourWheel > 0) {
                fourWheel--;
            } else {
                throw new IllegalStateException("No available 4W slots.");
            }
        }
    }

    // --- Release a Slot (e.g., after cancellation) ---
    public void releaseSlot(String vehicleType) {
        if (vehicleType.equalsIgnoreCase("2W") && twoWheel < totalTwoWheelSlots) {
            twoWheel++;
        } else if (vehicleType.equalsIgnoreCase("4W") && fourWheel < totalFourWheelSlots) {
            fourWheel++;
        }
    }

    // --- Admin: Add Slots ---
    public void addSlots(String vehicleType, int count) {
        if (count <= 0) return;
        if (vehicleType.equalsIgnoreCase("2W")) {
            twoWheel += count;
            totalTwoWheelSlots += count;
        } else if (vehicleType.equalsIgnoreCase("4W")) {
            fourWheel += count;
            totalFourWheelSlots += count;
        }
    }

    // --- Admin: Remove Slots ---
    public void removeSlots(String vehicleType, int count) {
        if (count <= 0) return;

        if (vehicleType.equalsIgnoreCase("2W")) {
            int actualRemove = Math.min(count, twoWheel);
            twoWheel -= actualRemove;
            totalTwoWheelSlots -= count;
            if (totalTwoWheelSlots < 0) totalTwoWheelSlots = 0;
        } else if (vehicleType.equalsIgnoreCase("4W")) {
            int actualRemove = Math.min(count, fourWheel);
            fourWheel -= actualRemove;
            totalFourWheelSlots -= count;
            if (totalFourWheelSlots < 0) totalFourWheelSlots = 0;
        }
    }
}