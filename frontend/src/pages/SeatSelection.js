import React, { useState, useEffect } from "react";
import axios from "axios";

// Local storage helper functions
const getSeatsFromStorage = () => {
  try {
    const seats = localStorage.getItem("selectedSeats");
    return seats ? JSON.parse(seats) : [];
  } catch (e) {
    return [];
  }
};

const saveSeatsToStorage = (seats) => {
  localStorage.setItem("selectedSeats", JSON.stringify(seats));
};

// Reusable SVG seat icon
const SeatIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M4 18v3h3v-3h10v3h3v-6H4v3zm15-8h3v3h-3v-3zM2 10h3v3H2v-3zm15 3H7V5c0-1.1.9-2 2-2h6c1.1 0 2 .9 2 2v8z" />
  </svg>
);

// Individual seat display component
const Seat = ({ label, state, onClick }) => {
  const getStyles = () => {
    switch (state) {
      case "available":
        return {
          bg: "bg-blue-600 hover:bg-blue-500",
          cursor: "cursor-pointer",
        };
      case "locked":
        return {
          bg: "bg-emerald-500 hover:bg-emerald-400",
          cursor: "cursor-pointer",
        };
      case "booked":
        return { bg: "bg-gray-800", cursor: "cursor-not-allowed" };
      case "locked-by-other":
        return { bg: "bg-gray-500 opacity-50", cursor: "cursor-not-allowed" };
      default:
        return { bg: "bg-gray-700", cursor: "cursor-default" };
    }
  };
  const styles = getStyles();
  return (
    <button
      onClick={onClick}
      disabled={state === "booked" || state === "locked-by-other"}
      className={`
                ${styles.bg} ${styles.cursor} text-white w-16 h-16 rounded-xl
                flex flex-col items-center justify-center gap-0.5
                transition-all duration-200
                ${
                  state !== "booked" && state !== "locked-by-other"
                    ? "hover:scale-105"
                    : "opacity-60"
                }
            `}
    >
      <SeatIcon />
      <span className="text-[10px] font-bold">{label}</span>
    </button>
  );
};

// Booking confirmation modal popup
const BookingModal = ({
  isOpen,
  onClose,
  onBookingSuccess,
  userDetails,
  handleInputChange,
  selectedSeats,
}) => {
  const [isBooking, setIsBooking] = useState(false);

  const handleConfirmBooking = async () => {
    if (!userDetails.name || !userDetails.email || !userDetails.mobile) {
      alert("Please fill in all your details.");
      return;
    }
    setIsBooking(true);
    try {
      await axios.post("/api/seats/book", {
        ...userDetails,
        seatNumbers: selectedSeats,
      });
      alert("Booking successful! A confirmation has been sent.");
      onBookingSuccess();
    } catch (error) {
      console.error("Booking failed:", error);
      alert(
        error.response?.data?.message || "Booking failed. Please try again."
      );
      onClose();
    } finally {
      setIsBooking(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-gray-900 text-white rounded-lg p-8 max-w-md w-full">
        <h2 className="text-2xl font-bold mb-4">Confirm Your Booking</h2>
        <p className="mb-6">
          Seats:{" "}
          <span className="font-bold text-emerald-400">
            {selectedSeats.join(", ")}
          </span>
        </p>
        <div className="space-y-4">
          <input
            type="text"
            name="name"
            placeholder="Full Name"
            value={userDetails.name}
            onChange={handleInputChange}
            className="w-full bg-gray-800 p-3 rounded"
          />
          <input
            type="email"
            name="email"
            placeholder="Email Address"
            value={userDetails.email}
            onChange={handleInputChange}
            className="w-full bg-gray-800 p-3 rounded"
          />
          <input
            type="tel"
            name="mobile"
            placeholder="Mobile Number"
            value={userDetails.mobile}
            onChange={handleInputChange}
            className="w-full bg-gray-800 p-3 rounded"
          />
        </div>
        <div className="flex justify-end gap-4 mt-8">
          <button
            onClick={onClose}
            disabled={isBooking}
            className="bg-gray-700 hover:bg-gray-600 font-medium py-2 px-4 rounded"
          >
            Cancel
          </button>
          <button
            onClick={handleConfirmBooking}
            disabled={isBooking}
            className="bg-emerald-500 hover:bg-emerald-400 font-medium py-2 px-4 rounded"
          >
            {isBooking ? "Booking..." : `Confirm & Book`}
          </button>
        </div>
      </div>
    </div>
  );
};

// Main seat selection page
const SeatSelection = () => {
  const [seatStates, setSeatStates] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [userDetails, setUserDetails] = useState({
    name: "",
    email: "",
    mobile: "",
  });

  // Initial data fetch effect
  useEffect(() => {
    const reconcileSeats = async () => {
      try {
        const [seatRes, lockedRes] = await Promise.all([
          axios.get("/api/seats"),
          axios.get("/api/seats/locked"),
        ]);

        const serverLockedSeats = new Set(lockedRes.data);
        const userSelectedSeats = new Set(getSeatsFromStorage());

        const finalSeatStates = seatRes.data.reduce((acc, seat) => {
          const seatNum = seat.seatNumber;
          if (seat.booked) {
            acc[seatNum] = "booked";
          } else if (userSelectedSeats.has(seatNum)) {
            if (serverLockedSeats.has(seatNum)) {
              acc[seatNum] = "locked";
            } else {
              acc[seatNum] = "available";
            }
          } else if (serverLockedSeats.has(seatNum)) {
            acc[seatNum] = "locked-by-other";
          } else {
            acc[seatNum] = "available";
          }
          return acc;
        }, {});

        setSeatStates(finalSeatStates);
        const validSelections = Object.keys(finalSeatStates).filter(
          (s) => finalSeatStates[s] === "locked"
        );
        saveSeatsToStorage(validSelections);
      } catch (error) {
        console.error("Failed to fetch seat data:", error);
      }
    };
    reconcileSeats();
  }, []);

  // Handles seat click logic
  const handleSeatClick = async (seatId) => {
    const currentState = seatStates[seatId];
    if (currentState === "booked" || currentState === "locked-by-other") return;

    const currentSelection = getSeatsFromStorage();

    if (currentState === "locked") {
      try {
        await axios.post("/api/seats/unlock", {
          seatNumber: seatId,
        });
        setSeatStates((prev) => ({ ...prev, [seatId]: "available" }));
        saveSeatsToStorage(currentSelection.filter((s) => s !== seatId));
      } catch (error) {
        console.error("Failed to unlock seat:", error);
        alert("Could not unlock seat. Please refresh.");
      }
      return;
    }

    if (currentState === "available") {
      try {
        await axios.post("/api/seats/select", {
          seatNumber: seatId,
        });
        setSeatStates((prev) => ({ ...prev, [seatId]: "locked" }));
        saveSeatsToStorage([...currentSelection, seatId]);
      } catch (error) {
        console.error("Failed to lock seat:", seatId, error);
        alert(
          error.response?.data?.message || `Seat ${seatId} was just taken.`
        );
        setSeatStates((prev) => ({ ...prev, [seatId]: "locked-by-other" }));
        saveSeatsToStorage(currentSelection.filter((s) => s !== seatId));
      }
    }
  };

  // Handles form input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setUserDetails((prev) => ({ ...prev, [name]: value }));
  };

  // Opens the booking modal
  const handleContinue = () => {
    if (selectedSeats.length > 0) setIsModalOpen(true);
    else alert("Please select at least one seat.");
  };

  // Resets user's current selection
  const handleReset = async () => {
    const lockedSeats = getSeatsFromStorage();
    for (const seatId of lockedSeats) {
      try {
        await axios.post("/api/seats/unlock", {
          seatNumber: seatId,
        });
      } catch (err) {
        console.error("Failed to unlock seat:", seatId, err);
      }
    }
    localStorage.removeItem("selectedSeats");
    const [seatRes, lockedRes] = await Promise.all([
      axios.get("/api/seats"),
      axios.get("/api/seats/locked"),
    ]);
    const serverLockedSeats = new Set(lockedRes.data);
    const transformedSeats = seatRes.data.reduce((acc, seat) => {
      if (seat.booked) acc[seat.seatNumber] = "booked";
      else if (serverLockedSeats.has(seat.seatNumber))
        acc[seat.seatNumber] = "locked-by-other";
      else acc[seat.seatNumber] = "available";
      return acc;
    }, {});
    setSeatStates(transformedSeats);
  };

  // Handles successful booking event
  const handleBookingSuccess = () => {
    setSeatStates((prev) => {
      const newStates = { ...prev };
      getSeatsFromStorage().forEach((seat) => {
        newStates[seat] = "booked";
      });
      return newStates;
    });
    localStorage.removeItem("selectedSeats");
    setIsModalOpen(false);
  };

  // Renders one row of seats
  const renderRow = (row) => {
    const seats = [];
    for (let i = 1; i <= 10; i++) {
      const seatId = `${row}${i}`;
      seats.push(
        <Seat
          key={seatId}
          label={seatId}
          state={seatStates[seatId]}
          onClick={() => handleSeatClick(seatId)}
        />
      );
      if (i === 5) seats.push(<div key={`${row}-aisle`} className="w-8" />);
    }
    return seats;
  };

  // Main component render logic
  const selectedSeats = getSeatsFromStorage();
  const selectedCount = selectedSeats.length;
  const availableCount = Object.values(seatStates).filter(
    (s) => s === "available"
  ).length;
  const bookedCount = Object.values(seatStates).filter(
    (s) => s === "booked" || s === "locked-by-other"
  ).length;
  const rows = ["A", "B", "C", "D", "E"];

  return (
    <div className="min-h-screen bg-black flex flex-col items-center justify-center px-4 py-6">
      <div className="w-full max-w-5xl space-y-8">
        <div className="text-center">
          <h1 className="text-white text-3xl font-semibold mb-6">
            Select Your Seats
          </h1>
          <div className="flex gap-4 justify-center flex-wrap text-sm">
            <div className="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg">
              <div className="w-7 h-7 bg-blue-600 rounded flex items-center justify-center">
                <SeatIcon />
              </div>
              <span className="text-gray-200">
                Available ({availableCount})
              </span>
            </div>
            <div className="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg">
              <div className="w-7 h-7 bg-emerald-500 rounded flex items-center justify-center">
                <SeatIcon />
              </div>
              <span className="text-gray-200">
                Your Selection ({selectedCount})
              </span>
            </div>
            <div className="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg">
              <div className="w-7 h-7 bg-gray-700 rounded flex items-center justify-center opacity-50">
                <SeatIcon />
              </div>
              <span className="text-gray-200">Booked ({bookedCount})</span>
            </div>
          </div>
        </div>
        <div>
          <div className="h-12 bg-gray-800 rounded-t-[100px] flex items-center justify-center">
            <span className="text-gray-300 text-sm tracking-[0.4em]">
              MOVIE SCREEN
            </span>
          </div>
        </div>
        <div className="flex flex-col gap-2">
          {rows.map((row) => (
            <div
              key={row}
              className="flex gap-2 items-center justify-center text-sm"
            >
              <span className="text-gray-300 font-semibold w-6 text-center">
                {row}
              </span>
              {renderRow(row)}
              <span className="text-gray-300 font-semibold w-6 text-center">
                {row}
              </span>
            </div>
          ))}
        </div>
        {selectedCount > 0 && (
          <div className="flex justify-center gap-4">
            <button
              onClick={handleReset}
              className="bg-gray-700 hover:bg-gray-600 text-white font-medium py-2 px-6 rounded-lg"
            >
              Reset
            </button>
            <button
              onClick={handleContinue}
              className="bg-emerald-500 hover:bg-emerald-400 text-white font-medium py-2 px-8 rounded-lg"
            >
              Continue ({selectedCount})
            </button>
          </div>
        )}
      </div>
      <BookingModal
        isOpen={isModalOpen}
        onClose={handleReset}
        onBookingSuccess={handleBookingSuccess}
        userDetails={userDetails}
        handleInputChange={handleInputChange}
        selectedSeats={selectedSeats}
      />
    </div>
  );
};

export default SeatSelection;
