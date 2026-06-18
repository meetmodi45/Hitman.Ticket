# Hitman.Ticket (TicketPlatform)

A full-stack, modern ticket booking and seat selection platform. This application allows users to view seat maps, select their desired seats, proceed to payment, and receive booking confirmations. It also includes admin capabilities for managing the seat map.

## 📸 Screenshots

Here is a glimpse of the application in action:

<div style="display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;">
  <img src="frontend/public/images/1.png" alt="Screenshot 1" width="400" style="border: 1px solid #ddd; border-radius: 8px;" />
  <img src="frontend/public/images/2.png" alt="Screenshot 2" width="400" style="border: 1px solid #ddd; border-radius: 8px;" />
  <img src="frontend/public/images/3.png" alt="Screenshot 3" width="400" style="border: 1px solid #ddd; border-radius: 8px;" />
  <img src="frontend/public/images/4.png" alt="Screenshot 4" width="400" style="border: 1px solid #ddd; border-radius: 8px;" />
  <img src="frontend/public/images/5.png" alt="Screenshot 5" width="400" style="border: 1px solid #ddd; border-radius: 8px;" />
</div>

## 🚀 Tech Stack

### Frontend
- **React.js**: User interface built with React (Create React App).
- **Tailwind CSS**: For responsive and modern styling.
- **React Router DOM**: Handling navigation between seat selection and admin pages.
- **Axios**: Making HTTP requests to the backend API.
- **Lucide React**: Beautiful icons.

### Backend
- **Java 17 & Spring Boot 3**: Robust and scalable backend framework.
- **Spring Data JPA**: ORM for database interactions.
- **PostgreSQL**: Primary relational database for persisting users, tickets, and seat states.
- **Redis**: In-memory data store, used for fast retrieval and managing temporary seat locks to prevent double-booking during checkout.
- **RabbitMQ**: Message broker for handling asynchronous tasks like sending email confirmations or processing PDF ticket generation.
- **iText**: For dynamically generating PDF tickets.
- **Java Mail Sender**: Sending confirmation emails.
- **Docker & Docker Compose**: Containerizing the backing services (Postgres, Redis, RabbitMQ) for easy setup.

## ⚙️ Implementation Details

### Redis (Temporary Seat Locking)
Redis is utilized to handle the concurrency of seat bookings and prevent double-booking scenarios.
- **Implementation**: The backend uses `RedisTemplate` within a dedicated `RedisService`.
- **How it works**: When a user selects a seat and proceeds to payment, `RedisService.tryLockSeat()` is called to temporarily lock the seat by setting a key-value pair (`setIfAbsent`) with a TTL (Time-To-Live). If another user attempts to book the same seat, the system checks Redis and denies the action.
- **Unlock**: Once the payment succeeds or times out, the seat is either permanently booked in PostgreSQL or the lock is released via `unlockSeat()`.

### RabbitMQ (Asynchronous Event Processing)
RabbitMQ acts as the message broker, decoupling core booking logic from time-consuming secondary tasks.
- **Implementation**: After a successful booking, a `BookingEvent` is published to RabbitMQ. The `BookingEventConsumer` listens to multiple queues using the `@RabbitListener` annotation.
- **Queues Handled**:
  - `EMAIL_QUEUE`: Triggers `EmailService.sendBookingConfirmationEmail()` to format and send an email asynchronously.
  - `INVOICE_QUEUE`: Delegates to `InvoiceService.generateInvoice()` which uses iText to dynamically construct a PDF ticket.
  - `SMS_QUEUE`: Triggers SMS notifications.

## 📂 Project Structure

```text
Hitman.Ticket/
├── backend/                  # Spring Boot application
│   ├── src/                  # Java source code
│   ├── pom.xml               # Maven dependencies
│   ├── docker-compose.yml    # Docker services (Postgres, Redis, RabbitMQ)
│   └── Dockerfile            # Docker configuration for Spring Boot
├── frontend/                 # React application
│   ├── src/                  # React components, pages, and logic
│   ├── public/               # Static assets including images
│   ├── package.json          # Node.js dependencies
│   └── tailwind.config.js    # Tailwind configuration
└── README.md                 # Project documentation
```

## 🛠️ Getting Started

Follow these instructions to get the project up and running on your local machine.

### Prerequisites
- [Java 17](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [Node.js](https://nodejs.org/)
- [Docker](https://www.docker.com/)

### 1. Start Backend Infrastructure
The backend relies on PostgreSQL, Redis, and RabbitMQ. You can start all of these easily using Docker Compose.

```bash
cd backend
docker-compose up -d
```
*This will start Postgres on port 5433 (mapped from 5432), Redis on 6379, and RabbitMQ on 5672.*

### 2. Run the Spring Boot Backend
From the `backend` directory, use Maven to start the application:

```bash
./mvnw spring-boot:run
```
*The backend server will run on `http://localhost:8080`.*

### 3. Run the React Frontend
Open a new terminal window and navigate to the `frontend` directory. Install the dependencies and start the development server:

```bash
cd frontend
npm install
npm run dev
```
*The frontend application will run on `http://localhost:3000`.*

## ✨ Features
- **Interactive Seat Map**: Users can see available, booked, and selected seats in real-time.
- **Temporary Seat Locking**: Seats are temporarily locked in Redis when a user proceeds to checkout, preventing concurrency issues.
- **Admin Dashboard**: Admins can manage the layout and overall state of the seat map.
- **Payment Integration**: A modal flow for handling checkout and payments.
- **Asynchronous Notifications**: RabbitMQ handles background processing for email confirmations and ticket (PDF) generation.
