import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FaShoppingCart } from 'react-icons/fa';
import SearchBar from '../components/SearchBar';

const Header: React.FC = () => {
  const navigate = useNavigate();

  const handleSearch = (query: string) => {
    navigate(`/?query=${encodeURIComponent(query)}`);
  };

  return (
    <header className="flex justify-between items-center px-4 py-3 bg-gray-100 shadow">
      <h1
        className="text-2xl font-bold cursor-pointer"
        onClick={() => navigate('/')}
      >
        Ecommerce Platform
      </h1>

      <div className="flex items-center gap-4">
        <SearchBar onSearch={handleSearch} />

        <button
          onClick={() => navigate('/address')}
          className="bg-gray-300 text-black px-4 py-2 rounded hover:bg-gray-400"
        >
          Delivering To
        </button>

        <button
          onClick={() => navigate('/cart')}
          className="flex items-center text-blue-600 hover:text-blue-800"
        >
          <FaShoppingCart size={24} />
        </button>

        <button
          onClick={() => navigate('/login')}
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          Sign In
        </button>
      </div>
    </header>
  );
};

export default Header;