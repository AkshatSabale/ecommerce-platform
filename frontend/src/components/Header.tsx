import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaShoppingCart, FaUserCircle, FaChevronDown, FaChevronUp } from 'react-icons/fa';
import SearchBar from '../components/SearchBar';
import { useAuth } from '../context/AuthContext';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const { isAuthenticated, logout, userId, loading } = useAuth();

  const handleSearch = (query: string) => {
    navigate(`/?query=${encodeURIComponent(query)}`);
  };

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
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
                    navigate('/transactions');
                    setIsDropdownOpen(false);
                  }}
                  className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                >
                  My Transactions
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
        )}
      </div>
    </header>
  );
};

export default Header;
