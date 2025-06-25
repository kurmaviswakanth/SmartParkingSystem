# Smart Parking Slot Booking System

_A console-based Java application to manage 2-wheeler and 4-wheeler parking slot reservations with real-time booking, check-in/out, overstay tracking, and admin controls._

![Smart Parking Slot Booking System](https://github.com/kurmaviswakanth/SmartParkingSystem/blob/main/Smart%20Parking%20Slot%20Booking%20System.jpg?raw=true)

---

## 📘 Project Overview

The **Smart Parking Slot Booking System** allows users to register, log in, book/cancel/extend parking slots, and check in/out vehicles. Admins can manage lots, update slots, waive penalties, and track revenue—all from a terminal-based interface.

---

## ✨ Key Features

### 👤 User Features
- **User Registration & Login**
- **OTP-based Password Reset**
- **View Parking Lots with Slot & Rate Details**
- **Book Parking Slot** (with 50% advance payment)
- **Check-In Vehicle** (up to 30 min early)
- **Cancel Booking** (grace period applies)
- **Extend Booking Duration**
- **Exit Vehicle & Pay Remaining Charges**
- **View Active Bookings**

### 🛠️ Admin Features
- **Secure Admin Login**
- **Add/Edit/Delete Parking Lots**
- **Add/Remove Parking Slots**
- **Waive Overstay Penalties**
- **View All Bookings**
- **Cancel User Bookings**
- **Track Revenue**

### 🔔 System Behaviors
- **Grace Period**: 10 minutes (for cancellations and overstays)
- **Cancellation Fees**: ₹20 (2W) / ₹30 (4W)
- **Overstay Charges**: ₹40 (2W) / ₹60 (4W) per 30 minutes
- **Reminders**: Background process alerts users of upcoming expiry
- **ANSI Console Colors** for better terminal UX

---

## 🗂 Project Structure

```bash
com/
└── parkingsystem/
    ├── SmartParking.java        # Main menu
    ├── User.java                # User model
    ├── OtpService.java          # OTP handling
    ├── UserBooking.java         # Booking features
    ├── UserService.java         # User auth logic
    ├── AdminService.java        # Admin menu and controls
    ├── ExitService.java         # Checkout & billing
    ├── Booking.java             # Booking data
    ├── Lot.java                 # Lot properties & pricing
    ├── DataStore.java           # Shared in-memory storage
    └── AnsiColors.java          # Console formatting
```
## ⚙️ Requirements

- Java JDK 8 or higher  
- Any Java IDE (IntelliJ, Eclipse, etc.) or terminal

---

## 🚀 Running the Application

```bash
javac com/parkingsystem/*.java
java com.parkingsystem.SmartParking
```

---

## 🧭 Usage Instructions

### Main Menu Options

1. **User Login**  
2. **User Registration**  
3. **Admin Login**  
4. **View Rules and Charges**  
5. **Exit**

---

## 🔡 Input Formats

| Field           | Format                    | Example     |
|----------------|---------------------------|-------------|
| Vehicle Number | `[A-Z]{2}\d{2}[A-Z]{2}\d{4}` | TS09AA1234  |
| Date           | `yyyy-MM-dd`              | 2025-06-25  |
| Time           | `HH:mm` (24-hr format)    | 14:30       |
| Phone Number   | 10-digit                  | 9441234567  |

---

## 🅿️ Sample Parking Lots

| Lot ID | Name                 | Location       | 2W/4W Slots | 2W/4W Price (₹/hr) |
|--------|----------------------|----------------|-------------|--------------------|
| L001   | Hitech Parking       | Hitech City    | 50 / 20     | 30 / 60            |
| L002   | RGIA Airport         | Shamshabad     | 60 / 25     | 50 / 80            |
| L003   | Hyderabad Deccan     | Nampally       | 45 / 18     | 35 / 55            |
| L004   | Gachibowli Hub       | Gachibowli     | 50 / 22     | 28 / 50            |
| L005   | Secunderabad Station | Secunderabad   | 55 / 20     | 32 / 52            |

---

## ❗ Limitations

- Console UI only (no GUI)  
- No persistent storage (in-memory only)  
- One active booking per vehicle  
- OTP displayed in terminal (not sent via SMS)

---

## 💡 Future Enhancements

- GUI using JavaFX or Swing  
- Persistent data storage using files or database  
- SMS/email integration for OTP & reminders  
- Online payment support  
- Booking history and multiple vehicle profiles  
- Admin reports and analytics dashboard

---

## 📝 Notes

Ensure your terminal supports ANSI escape codes for color formatting.

**Default Admin Credentials:**

- **Username:** `admin`  
- **Password:** `admin123`  
- **Phone:** `9441234567`

Grace period is configured in **`DataStore.java`**.


---

## ✅ Conclusion

The **Smart Parking Slot Booking System** offers a practical and modular approach to managing parking spaces in urban environments. By leveraging a Java-based console interface, it simulates a real-world reservation and management system complete with slot tracking, overstay penalties, user authentication, and admin oversight.

While the current version operates in-memory and through the terminal, it lays a solid foundation for future enhancements such as GUI integration, persistent storage, and real-time notifications. Ideal for academic, learning, or prototype use cases, this system showcases object-oriented programming, modular design, and effective user flow management in Java.

With further development, it can evolve into a scalable parking management solution for smart cities and public infrastructure.

---

