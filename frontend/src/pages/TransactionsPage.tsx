import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';
import { FaChevronLeft, FaChevronRight, FaReceipt } from 'react-icons/fa';

interface Payment {
  id: number;
  paymentId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
  orderId?: number;
}

const TransactionsPage: React.FC = () => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();

  const fetchPayments = async () => {
    setLoading(true);
    try {
      const response = await api.get('/payments/user', {
        params: {
          page,
          size: 5,
        },
      });
      setPayments(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Failed to fetch payments:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [page]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'captured':
        return 'bg-green-50 text-green-700 border-green-200';
      case 'failed':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'pending':
        return 'bg-yellow-50 text-yellow-700 border-yellow-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  return (
    <div className="container mx-auto p-4 max-w-4xl">
      <div className="flex items-center mb-6">
        <FaReceipt className="text-blue-600 mr-2 text-xl" />
        <h1 className="text-2xl font-bold text-gray-800">Transaction History</h1>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      ) : payments.length === 0 ? (
        <div className="bg-gray-50 rounded-lg p-8 text-center">
          <div className="text-gray-400 mb-4">
            <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-700 mb-1">No transactions found</h3>
          <p className="text-gray-500">Your transaction history will appear here</p>
        </div>
      ) : (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            {payments.map((payment) => (
              <div
                key={payment.id}
                className="p-5 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 transition-colors duration-150"
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center">
                    <div className={`w-3 h-3 rounded-full mr-3 ${
                      payment.status === 'captured' ? 'bg-green-500' :
                      payment.status === 'failed' ? 'bg-red-500' : 'bg-yellow-500'
                    }`}></div>
                    <div>
                      <span className="font-semibold text-gray-900">₹{payment.amount.toLocaleString('en-IN')}</span>
                      <span className={`ml-3 text-xs px-2 py-1 rounded-full ${getStatusColor(payment.status)} border`}>
                        {payment.status.charAt(0).toUpperCase() + payment.status.slice(1)}
                      </span>
                    </div>
                  </div>
                  <div className="text-xs text-gray-500 bg-gray-50 px-2 py-1 rounded">
                    {formatDate(payment.createdAt)}
                  </div>
                </div>
                <div className="text-sm text-gray-600 mb-3 ml-6">ID: {payment.paymentId}</div>
                {payment.orderId && (
                  <div className="ml-6">
                    <button
                      onClick={() => navigate(`/orders/${payment.orderId}`)}
                      className="text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center"
                    >
                      View Order Details
                      <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path>
                      </svg>
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-between items-center mt-6 px-2">
              <button
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={page === 0}
                className={`flex items-center px-4 py-2 rounded-md ${
                  page === 0
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-white text-blue-600 hover:bg-blue-50 border border-gray-200'
                }`}
              >
                <FaChevronLeft className="mr-1" />
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                disabled={page + 1 >= totalPages}
                className={`flex items-center px-4 py-2 rounded-md ${
                  page + 1 >= totalPages
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-white text-blue-600 hover:bg-blue-50 border border-gray-200'
                }`}
              >
                Next
                <FaChevronRight className="ml-1" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default TransactionsPage;