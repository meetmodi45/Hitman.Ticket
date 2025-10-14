import React from "react";
import { Routes, Route } from "react-router-dom";
import SeatSelection from "./pages/SeatSelection";
import AdminSeatMap from "./pages/AdminSeatMap";

function App() {
  return (
    <Routes>
      <Route path="/admin" element={<AdminSeatMap />} />
      <Route path="/" element={<SeatSelection />} />
    </Routes>
  );
}

export default App;
