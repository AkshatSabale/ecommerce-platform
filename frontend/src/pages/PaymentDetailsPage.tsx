import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';

interface Payment {
  id: number;
  paymentId: string;
  razorpayOrderId: string;
  amount: number;
  currency: string;
  status: string;
  method?: string;
  bank?: string;
  wallet?: string;
  cardId?: string;
  createdAt: string;
  order?: {
    id: number;
  };
}

const PaymentDetailsPage: React.FC = () => {
  const { paymentId } = useParams<{ paymentId: string }>();
  const navigate = useNavigate();
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPayment = async () => {
      try {
        const response = await api.get(`api/payment/${paymentId}`);
        setPayment(response.data);
      } catch (err: any) {
        setError('Failed to load payment details. Please try again later.');
        console.error('Error fetching payment details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPayment();
  }, [paymentId]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
    </div>
  );

  if (error) return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-red-50 border-l-4 border-red-500 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    </div>
  );

  if (!payment) return (
    <div className="max-w-4xl mx-auto p-6 text-center">
      <div className="bg-blue-50 border-l-4 border-blue-500 p-4">
        <p className="text-blue-700">No payment found with this ID.</p>
      </div>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Payment Details</h1>

      <div className="space-y-6">
        {/* Payment Summary */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <div className="flex justify-between items-start mb-4">
            <div>
              <h2 className="text-xl font-semibold text-gray-800">Payment #{payment.paymentId || payment.id}</h2>
              <p className="text-gray-500 text-sm mt-1">
                {formatDate(payment.createdAt)}
              </p>
            </div>
            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
              payment.status === 'captured'
                ? 'bg-green-100 text-green-800'
                : payment.status === 'failed'
                  ? 'bg-red-100 text-red-800'
                  : 'bg-yellow-100 text-yellow-800'
            }`}>
              {payment.status}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 className="text-sm font-medium text-gray-500">Amount</h3>
              <p className="text-xl font-semibold text-gray-800 mt-1">
                ₹{payment.amount.toFixed(2)} {payment.currency}
              </p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">Payment Method</h3>
              <p className="text-gray-800 mt-1">
                {payment.method ? `${payment.method}${payment.bank ? ` (${payment.bank})` : ''}` : 'N/A'}
              </p>
            </div>
          </div>
        </div>

        {/* Payment Details */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">Transaction Details</h2>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">Razorpay Order ID</span>
              <span className="text-gray-800">{payment.razorpayOrderId || 'N/A'}</span>
            </div>
            {payment.cardId && (
              <div className="flex justify-between">
                <span className="text-gray-600">Card ID</span>
                <span className="text-gray-800">{payment.cardId}</span>
              </div>
            )}
            {payment.wallet && (
              <div className="flex justify-between">
                <span className="text-gray-600">Wallet</span>
                <span className="text-gray-800">{payment.wallet}</span>
              </div>
            )}
          </div>
        </div>

        {/* Related Order */}
        {payment.order?.id && (
          <div className="bg-gray-50 p-6 rounded-lg">
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-lg font-semibold text-gray-800">Related Order</h2>
                <p className="text-gray-600 text-sm mt-1">Order #{payment.order.id}</p>
              </div>
              <button
                onClick={() => navigate(`/orders/${payment.order?.id}`)}
                className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition-colors"
              >
                View Order
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentDetailsPage;