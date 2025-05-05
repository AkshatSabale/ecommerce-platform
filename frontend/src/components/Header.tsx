import React from 'react';
import { useNavigate } from 'react-router-dom';

const Header: React.FC = () => {
  const navigate = useNavigate();

  return (
    <header className="flex justify-between items-center px-4 py-3 bg-gray-100 shadow">
      <h1 className="text-2xl font-bold">Ecommerce Platform</h1>
      <button
        onClick={() => navigate('/login')}
        className="bg-blue-500 text-white px-4 py-2 rounded"
      >
        Sign In
      </button>
    </header>
  );
};

export default Header;