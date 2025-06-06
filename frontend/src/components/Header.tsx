import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaShoppingCart, FaUserCircle, FaChevronDown, FaChevronUp, FaMoneyBillWave } from 'react-icons/fa';
import SearchBar from '../components/SearchBar';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface Payment {
  id: number;
  paymentId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
  order?: {
    id: number;
  };
}

const Header: React.FC = () => {
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isTransactionsOpen, setIsTransactionsOpen] = useState(false);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loadingPayments, setLoadingPayments] = useState(false);
  const { isAuthenticated, logout, userId, loading } = useAuth();

  const handleSearch = (query: string) => {
    navigate(`/?query=${encodeURIComponent(query)}`);
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
    setIsTransactionsOpen(false);
  };

  const toggleTransactions = async () => {
    if (!isTransactionsOpen && isAuthenticated) {
      setLoadingPayments(true);
      try {
        const response = await api.get('/payments/user');
        setPayments(response.data);
      } catch (error) {
        console.error('Failed to fetch payments:', error);
      } finally {
        setLoadingPayments(false);
      }
    }
    setIsTransactionsOpen(!isTransactionsOpen);
    setIsDropdownOpen(false);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  };

  if (loading) {
    return null;
  }

  return (
    <header className="flex justify-between items-center px-4 py-3 bg-gray-100 shadow relative">
      {/* Left side - Logo */}
      <h1
        className="text-2xl font-bold cursor-pointer"
        onClick={() => navigate('/')}
      >
        Ecommerce Platform
      </h1>

      {/* Middle - Search bar */}
      <div className="flex-grow mx-8">
        <SearchBar onSearch={handleSearch} />
      </div>

      {/* Right side - Navigation */}
      <div className="flex items-center gap-4">
        {!isAuthenticated ? (
          <button
            onClick={() => navigate('/login')}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition-colors"
          >
            Sign In
          </button>
        ) : (
          <>
            <div className="relative">
              <button
                onClick={toggleTransactions}
                className="flex items-center gap-2 hover:bg-gray-200 px-3 py-2 rounded transition-colors"
              >
                <FaMoneyBillWave size={20} />
                <span>Transactions</span>
                {isTransactionsOpen ? <FaChevronUp /> : <FaChevronDown />}
              </button>

              {isTransactionsOpen && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-md shadow-lg py-1 z-10 max-h-96 overflow-y-auto">
                  {loadingPayments ? (
                    <div className="px-4 py-2 text-sm text-gray-700 flex justify-center">
                      <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-blue-500"></div>
                    </div>
                  ) : payments.length === 0 ? (
                    <div className="px-4 py-2 text-sm text-gray-700">
                      No transactions found
                    </div>
                  ) : (
                    <>
                      <div className="px-4 py-2 text-sm font-medium text-gray-700 border-b border-gray-200">
                        My Transactions
                      </div>
                      {payments.map((payment) => (
                        <div key={payment.id} className="border-b border-gray-100 last:border-0">
                          <button
                            onClick={() => {
                              navigate(`/payments/${payment.paymentId || payment.id}`);
                              setIsTransactionsOpen(false);
                            }}
                            className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                          >
                            <div className="flex justify-between">
                              <span className="font-medium">₹{payment.amount}</span>
                              <span className={`text-xs px-2 py-1 rounded ${
                                payment.status === 'captured'
                                  ? 'bg-green-100 text-green-800'
                                  : payment.status === 'failed'
                                    ? 'bg-red-100 text-red-800'
                                    : 'bg-yellow-100 text-yellow-800'
                              }`}>
                                {payment.status}
                              </span>
                            </div>
                            <div className="text-xs text-gray-500 mt-1">
                              {formatDate(payment.createdAt)}
                            </div>
                            {payment.order?.id && (
                              <div className="mt-1">
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(`/orders/${payment.order?.id}`);
                                    setIsTransactionsOpen(false);
                                  }}
                                  className="text-xs text-blue-600 hover:text-blue-800 hover:underline"
                                >
                                  View Order
                                </button>
                              </div>
                            )}
                          </button>
                        </div>
                      ))}
                    </>
                  )}
                </div>
              )}
            </div>

            <div className="relative">
              <button
                onClick={toggleDropdown}
                className="flex items-center gap-2 hover:bg-gray-200 px-3 py-2 rounded transition-colors"
              >
                <FaUserCircle size={24} />
                <span>Account</span>
                {isDropdownOpen ? <FaChevronUp /> : <FaChevronDown />}
              </button>

              {isDropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10">
                  <div className="px-4 py-2 text-sm text-gray-700 border-b border-gray-200">
                    User ID: {userId}
                  </div>
                  <button
                    onClick={() => {
                      navigate('/address');
                      setIsDropdownOpen(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Delivering To
                  </button>
                  <button
                    onClick={() => {
                      navigate('/cart');
                      setIsDropdownOpen(false);
                    }}
                    className="flex items-center gap-2 w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    <FaShoppingCart size={16} />
                    Cart
                  </button>
                  <button
                    onClick={() => {
                      navigate('/orders');
                      setIsDropdownOpen(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    My Orders
                  </button>
                  <button
                    onClick={() => {
                      navigate('/wishlist');
                      setIsDropdownOpen(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Wishlist
                  </button>
                  <button
                    onClick={() => {
                      logout();
                      setIsDropdownOpen(false);
                    }}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 border-t border-gray-200"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </header>
  );
};

export default Header;