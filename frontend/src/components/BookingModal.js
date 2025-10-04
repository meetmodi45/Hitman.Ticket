import React, { useState } from "react";
import axios from "axios";
import PaymentModal from "./PaymentModal";

const BookingModal = ({
  isOpen,
  onClose,
  onBookingSuccess,
  userDetails,
  handleInputChange,
  selectedSeats,
}) => {
  const [isPaymentOpen, setIsPaymentOpen] = useState(false);

  if (!isOpen) return null;

  // 1️⃣ Actual booking logic (same as current confirm)
  const handleBooking = async () => {
    const bookingRequest = {
      ...userDetails,
      seatNumbers: selectedSeats,
    };

    try {
      const response = await axios.post(
        "http://localhost:8080/api/seats/book",
        bookingRequest
      );
      alert(response.data);
      onBookingSuccess();
    } catch (error) {
      console.error("Booking failed:", error);
      let errorMessage = "An unknown error occurred.";
      if (error.response && error.response.data) {
        errorMessage =
          typeof error.response.data === "string"
            ? error.response.data
            : JSON.stringify(error.response.data);
      }
      alert("Error: " + errorMessage);
    }
  };

  // 2️⃣ Continue to pay click opens payment options
  const handleContinueToPay = () => {
    if (!userDetails.name || !userDetails.email || !userDetails.mobile) {
      alert("Please fill in all your details.");
      return;
    }
    setIsPaymentOpen(true);
  };

  // 3️⃣ Handle payment success/fail
  const handlePaymentResult = async (success) => {
    setIsPaymentOpen(false);

    if (success) {
      handleBooking(); // Payment success = actually book the seats
    } else {
      alert("❌ Payment failed! Seats released.");

      // Unlock all temporarily locked seats
      for (const seat of selectedSeats) {
        try {
          await axios.post(
            "http://localhost:8080/api/seats/unlock",
            { seatNumber: seat },
            { headers: { "Content-Type": "application/json" } }
          );
        } catch (err) {
          console.error("Failed to unlock seat in Redis:", seat, err);
        }
      }

      // Hard reload the page to reflect real-time seat status
      window.location.reload();
    }
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
        <div className="bg-gray-900 border border-gray-700 rounded-xl p-8 w-full max-w-md text-white">
          <h2 className="text-2xl font-bold mb-6 text-center">
            Your Information
          </h2>
          <div className="space-y-4">
            <input
              type="text"
              name="name"
              placeholder="Name"
              value={userDetails.name}
              onChange={handleInputChange}
              className="w-full bg-gray-800 border border-gray-600 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={userDetails.email}
              onChange={handleInputChange}
              className="w-full bg-gray-800 border border-gray-600 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <input
              type="tel"
              name="mobile"
              placeholder="Mobile Number"
              value={userDetails.mobile}
              onChange={handleInputChange}
              className="w-full bg-gray-800 border border-gray-600 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>
          <div className="mt-8 flex gap-4">
            <button
              onClick={onClose}
              className="w-full bg-gray-700 hover:bg-gray-600 font-medium py-3 rounded-lg"
            >
              Cancel
            </button>
            <button
              onClick={handleContinueToPay}
              className="w-full bg-emerald-500 hover:bg-emerald-400 font-medium py-3 rounded-lg"
            >
              Continue to Pay
            </button>
          </div>
        </div>
      </div>

      <PaymentModal
        isOpen={isPaymentOpen}
        onClose={() => setIsPaymentOpen(false)}
        onPaymentResult={handlePaymentResult}
      />
    </>
  );
};

export default BookingModal;
