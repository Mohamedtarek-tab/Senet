# UML Diagrams Logic - "Book a Car" Flow

## 1. Use Case Diagram Logic
**Actors**: Client, Admin, Payment Gateway (Simulated Backend)
**Use Cases**:
- **Client**: Login, View Cars, Select Car, Book Car, Pay for Booking.
- **Admin**: View Bookings, Update Booking Status.
- **Relationships**:
  - `Book Car` `<<includes>>` `Login` (Must be authenticated).
  - `Pay for Booking` `<<extends>>` `Book Car` (Optional/Follow-up step).

## 2. Class Diagram Logic
**Classes**:
- `User` (id, email, password, role)
- `Car` (id, brand, name, year, price, category, status, etc.)
- `Booking` (id, carId, clientName, pickup, ret, amount, status)
- `Payment` (id, bookingId, amount, status)
**Relationships**:
- User `1` <--> `0..*` Booking (One User can have many Bookings).
- Booking `1` <--> `1` Car (For this specific rental duration).
- Booking `1` <--> `1` Payment (One payment confirms a booking).

## 3. Sequence Diagram Logic ("Book a Car" Flow)
1. **Client -> Frontend**: User clicks "Book" on a Car card.
2. **Frontend -> Gateway (API)**: POST `/api/bookings` with Bearer Token.
3. **Gateway -> AuthFilter**: Validates JWT. Returns `userId` header.
4. **Gateway -> BookingService**: Forwards request to Booking Service.
5. **BookingService -> SQLite**: Saves booking with status "pending".
6. **BookingService -> Gateway**: Returns Booking details (including `id`).
7. **Gateway -> Frontend**: Displays Booking confirmation / Payment modal.
8. **Frontend -> Gateway**: POST `/api/payments` with `bookingId`.
9. **Gateway -> BookingService (PaymentController)**: Processes simulated payment.
10. **PaymentService -> BookingService**: Updates booking status to "confirmed".
11. **Gateway -> Frontend**: Success response. UI updates badge to 'Confirmed'.
