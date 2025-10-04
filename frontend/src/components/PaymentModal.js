import React from "react";

const PaymentModal = ({ isOpen, onClose, onPaymentResult }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div className="bg-gray-900 p-6 rounded-lg w-full max-w-sm text-white">
        <h2 className="text-xl font-bold mb-4 text-center">Payment Options</h2>
        <div className="flex flex-col gap-4">
          <button
            onClick={() => onPaymentResult(true)}
            className="bg-emerald-500 hover:bg-emerald-400 py-2 rounded-lg"
          >
            Payment Success
          </button>
          <button
            onClick={() => onPaymentResult(false)}
            className="bg-red-500 hover:bg-red-400 py-2 rounded-lg"
          >
            Payment Fail
          </button>
          <button
            onClick={onClose}
            className="bg-gray-700 hover:bg-gray-600 py-2 rounded-lg"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;
