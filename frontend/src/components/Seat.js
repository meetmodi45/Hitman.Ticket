import React from "react";

const SeatIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M4 18v3h3v-3h10v3h3v-6H4v3zm15-8h3v3h-3v-3zM2 10h3v3H2v-3zm15 3H7V5c0-1.1.9-2 2-2h6c1.1 0 2 .9 2 2v8z" />
  </svg>
);

const Seat = ({ label, state, onClick }) => {
  const getStyles = () => {
    switch (state) {
      case "available":
        return {
          bg: "bg-blue-600 hover:bg-blue-500",
          cursor: "cursor-pointer",
        };
      case "locked": // self-selected
        return {
          bg: "bg-emerald-500 hover:bg-emerald-400",
          cursor: "cursor-pointer",
        };
      case "booked": // permanently booked
        return {
          bg: "bg-gray-800",
          cursor: "cursor-not-allowed",
        };
      case "locked-by-other": // locked in Redis by someone else
        return {
          bg: "bg-gray-500 opacity-50",
          cursor: "cursor-not-allowed",
        };
      default:
        return {
          bg: "bg-gray-700",
          cursor: "cursor-default",
        };
    }
  };

  const styles = getStyles();

  return (
    <button
      onClick={onClick}
      disabled={state === "booked" || state === "locked-by-other"} // prevent clicks
      className={`
        ${styles.bg} ${styles.cursor}
        text-white w-16 h-16 rounded-xl
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

export default Seat;
