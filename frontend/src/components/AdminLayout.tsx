import React from 'react';
import { Outlet, useNavigate } from 'react-router-dom';

const AdminLayout = () => {
  const navigate = useNavigate();

  return (
    <div>
      <div className="flex space-x-4 p-4 bg-gray-100 border-b">
        <button
          onClick={() => navigate('/admin/dashboard')}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Product Management
        </button>
        <button
          onClick={() => navigate('/admin/analytics')}
          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          Product Analytics
        </button>
      </div>
      <div className="p-6">
        <Outlet />
      </div>
    </div>
  );
};

export default AdminLayout;