import React, { useState, useEffect } from "react";
import axios from "axios";
import Seat from "../components/Seat";
import BookingModal from "../components/BookingModal";

const SeatIcon = ({ className }) => (
  <svg
    className={className}
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="currentColor"
  >
    <path d="M4 18v3h3v-3h10v3h3v-6H4v3zm15-8h3v3h-3v-3zM2 10h3v3H2v-3zm15 3H7V5c0-1.1.9-2 2-2h6c1.1 0 2 .9 2 2v8z" />
  </svg>
);

const SeatSelection = () => {
  const [seatStates, setSeatStates] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [userDetails, setUserDetails] = useState({
    name: "",
    email: "",
    mobile: "",
  });

  useEffect(() => {
    fetchSeats();
  }, []);

  const fetchSeats = async () => {
    try {
      const [seatRes, lockedRes] = await Promise.all([
        axios.get("http://localhost:8080/api/seats"),
        axios.get("http://localhost:8080/api/seats/locked"),
      ]);
      const lockedSeats = new Set(lockedRes.data);
      const transformedSeats = seatRes.data.reduce((acc, seat) => {
        if (seat.booked) acc[seat.seatNumber] = "booked";
        else if (lockedSeats.has(seat.seatNumber))
          acc[seat.seatNumber] = "locked-by-other";
        else acc[seat.seatNumber] = "available";
        return acc;
      }, {});
      setSeatStates(transformedSeats);
    } catch (error) {
      console.error("Failed to fetch seat data:", error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setUserDetails((prev) => ({ ...prev, [name]: value }));
  };

  const handleContinue = () => {
    const selectedSeats = Object.keys(seatStates).filter(
      (s) => seatStates[s] === "locked"
    );
    if (selectedSeats.length > 0) {
      setIsModalOpen(true);
    } else {
      alert("Please select at least one seat.");
    }
  };

  const handleSeatClick = async (seatId) => {
    const currentState = seatStates[seatId];
    if (currentState === "booked" || currentState === "locked-by-other") return;

    if (currentState === "locked") {
      try {
        await axios.post("http://localhost:8080/api/seats/unlock", {
          seatNumber: seatId,
        });
        setSeatStates((prev) => ({ ...prev, [seatId]: "available" }));
      } catch (error) {
        console.error("Failed to unlock seat:", error);
        alert("Could not unlock seat. Please refresh.");
      }
      return;
    }

    if (currentState === "available") {
      try {
        await axios.post("http://localhost:8080/api/seats/select", {
          seatNumber: seatId,
        });
        setSeatStates((prev) => ({ ...prev, [seatId]: "locked" }));
      } catch (error) {
        console.error("Failed to lock seat:", error);
        alert(error.response?.data || "Failed to select seat.");
        fetchSeats();
      }
    }
  };

  const handleReset = async () => {
    const lockedSeats = Object.keys(seatStates).filter(
      (seatId) => seatStates[seatId] === "locked"
    );
    for (const seatId of lockedSeats) {
      try {
        await axios.post("http://localhost:8080/api/seats/unlock", {
          seatNumber: seatId,
        });
      } catch (err) {
        console.error("Failed to unlock seat:", seatId, err);
      }
    }
    fetchSeats();
  };

  const handleBookingSuccess = () => {
    setSeatStates((prev) => {
      const newStates = { ...prev };
      Object.keys(newStates).forEach((seat) => {
        if (newStates[seat] === "locked") {
          newStates[seat] = "booked";
        }
      });
      return newStates;
    });
    setIsModalOpen(false);
  };

  const renderRow = (row) => {
    const seats = [];
    const seatsPerRow = 10;
    for (let i = 1; i <= seatsPerRow; i++) {
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

  const selectedSeats = Object.keys(seatStates).filter(
    (s) => seatStates[s] === "locked"
  );
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
