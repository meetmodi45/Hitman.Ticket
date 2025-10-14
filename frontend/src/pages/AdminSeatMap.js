import React, { useState, useEffect } from "react";
import axios from "axios";

// --- Reusable SVG Icon ---
const SeatIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M4 18v3h3v-3h10v3h3v-6H4v3zm15-8h3v3h-3v-3zM2 10h3v3H2v-3zm15 3H7V5c0-1.1.9-2 2-2h6c1.1 0 2 .9 2 2v8z" />
  </svg>
);

// --- A New Tooltip Component to Show Booking Details ---
const Tooltip = ({ details, position }) => {
  if (!details) return null;

  const style = {
    position: "fixed",
    top: `${position.y + 15}px`,
    left: `${position.x + 15}px`,
    pointerEvents: "none", // Allows mouse events to pass through to elements below
  };

  return (
    <div
      style={style}
      className="z-50 p-4 bg-gray-900 border border-gray-700 text-white rounded-lg shadow-xl transition-opacity duration-200"
    >
      <h3 className="font-bold text-lg text-emerald-400">
        {details.seatNumber}
      </h3>
      <p className="text-sm">
        <strong>Booked By:</strong> {details.bookedByName}
      </p>
      <p className="text-sm">
        <strong>Email:</strong> {details.bookedByEmail}
      </p>
      <p className="text-sm">
        <strong>Mobile:</strong> {details.bookedByMobile}
      </p>
      <p className="text-sm">
        <strong>Booking Time:</strong>{" "}
        {new Date(details.bookingTimestamp).toLocaleString()}
      </p>
    </div>
  );
};

// --- A Simplified Seat Component for the Admin View (Read-Only) ---
const AdminSeat = ({ label, state, onMouseEnter, onMouseLeave }) => {
  const getStyles = () => {
    switch (state) {
      case "available":
        return "bg-blue-600";
      case "booked":
        return "bg-red-600"; // Changed to red for clear admin distinction
      default:
        return "bg-gray-700";
    }
  };

  return (
    <div
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      className={`
                ${getStyles()}
                text-white w-16 h-16 rounded-xl
                flex flex-col items-center justify-center gap-0.5
                transition-all duration-200
                ${state === "booked" ? "cursor-pointer" : "cursor-default"}
            `}
    >
      <SeatIcon />
      <span className="text-[10px] font-bold">{label}</span>
    </div>
  );
};

// --- The Main Admin Seat Map Component ---
const AdminSeatMap = () => {
  const [allSeats, setAllSeats] = useState({});
  const [bookedDetails, setBookedDetails] = useState({});
  const [hoveredSeat, setHoveredSeat] = useState(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });
  const [isLoading, setIsLoading] = useState(true);

  // Fetch data from the new admin endpoint on component mount
  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/seats/admin/booked-details"
        );
        const bookedData = response.data;

        // Create a lookup map for fast access to booking details by seat number
        const detailsMap = bookedData.reduce((acc, detail) => {
          acc[detail.seatNumber] = detail;
          return acc;
        }, {});
        setBookedDetails(detailsMap);

        // Create the master list of all 50 seats with their state
        const seatStates = {};
        const rows = ["A", "B", "C", "D", "E"];
        rows.forEach((row) => {
          for (let i = 1; i <= 10; i++) {
            const seatId = `${row}${i}`;
            seatStates[seatId] = detailsMap[seatId] ? "booked" : "available";
          }
        });
        setAllSeats(seatStates);
      } catch (error) {
        console.error("Failed to fetch admin booking data:", error);
        alert("Could not load booking data. Please check the console.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchAdminData();
  }, []);

  const handleMouseEnter = (seatId, event) => {
    if (allSeats[seatId] === "booked") {
      setHoveredSeat(seatId);
      setTooltipPosition({ x: event.clientX, y: event.clientY });
    }
  };

  const handleMouseLeave = () => {
    setHoveredSeat(null);
  };

  const renderRow = (row) => {
    const seats = [];
    for (let i = 1; i <= 10; i++) {
      const seatId = `${row}${i}`;
      seats.push(
        <AdminSeat
          key={seatId}
          label={seatId}
          state={allSeats[seatId]}
          onMouseEnter={(e) => handleMouseEnter(seatId, e)}
          onMouseLeave={handleMouseLeave}
        />
      );
      if (i === 5) seats.push(<div key={`${row}-aisle`} className="w-8" />);
    }
    return seats;
  };

  const availableCount = Object.values(allSeats).filter(
    (s) => s === "available"
  ).length;
  const bookedCount = Object.values(allSeats).filter(
    (s) => s === "booked"
  ).length;

  if (isLoading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center text-white text-2xl">
        Loading Admin Dashboard...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black flex flex-col items-center justify-center px-4 py-6">
      <Tooltip
        details={bookedDetails[hoveredSeat]}
        position={tooltipPosition}
      />
      <div className="w-full max-w-5xl space-y-8">
        <div className="text-center">
          <h1 className="text-white text-3xl font-semibold mb-6">
            Admin Seat Map & Occupancy
          </h1>
          <div className="flex gap-4 justify-center flex-wrap text-sm">
            {/* Legend */}
            <div className="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg">
              <div className="w-7 h-7 bg-blue-600 rounded flex items-center justify-center">
                <SeatIcon />
              </div>
              <span className="text-gray-200">
                Available ({availableCount})
              </span>
            </div>
            <div className="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg">
              <div className="w-7 h-7 bg-red-600 rounded flex items-center justify-center">
                <SeatIcon />
              </div>
              <span className="text-gray-200">Booked ({bookedCount})</span>
            </div>
          </div>
        </div>
        {/* Screen */}
        <div>
          <div className="h-12 bg-gray-800 rounded-t-[100px] flex items-center justify-center">
            <span className="text-gray-300 text-sm tracking-[0.4em]">
              MOVIE SCREEN
            </span>
          </div>
        </div>
        {/* Seat Grid */}
        <div className="flex flex-col gap-2">
          {["A", "B", "C", "D", "E"].map((row) => (
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
      </div>
    </div>
  );
};

export default AdminSeatMap;
