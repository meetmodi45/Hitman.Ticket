import React from "react";

const PaymentModal = ({ isOpen, onClose, onPaymentResult }) => {
  if (!isOpen) {
    return null;
  }

  const handleSuccess = () => onPaymentResult(true);
  const handleFailure = () => onPaymentResult(false);

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-md flex items-center justify-center z-50">
      <div className="bg-gray-800 border border-gray-600 rounded-xl p-8 w-full max-w-sm text-white text-center">
        <h2 className="text-2xl font-bold mb-4">Simulate Payment</h2>
        <p className="text-gray-400 mb-8">
          This is a placeholder for a real payment gateway.
        </p>
        <div className="space-y-4">
          <button
            onClick={handleSuccess}
            className="w-full bg-green-600 hover:bg-green-500 text-white font-medium py-3 rounded-lg transition-colors"
          >
            Simulate Successful Payment
          </button>
          <button
            onClick={handleFailure}
            className="w-full bg-red-600 hover:bg-red-500 text-white font-medium py-3 rounded-lg transition-colors"
          >
            Simulate Failed Payment
          </button>
          <button
            onClick={onClose}
            className="w-full text-gray-400 hover:text-white pt-2"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;
