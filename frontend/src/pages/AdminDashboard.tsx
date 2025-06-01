import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
/*
import AdminSidebar from '../components/AdminSidebar';
*/
const AdminDashboard = () => {
  const navigate = useNavigate();

  return (
    <div className="admin-layout">
      <div className="admin-content">
        <h1>Admin Dashboard</h1>
        {/* Add your admin components here */}
      </div>
    </div>
  );
};

export default AdminDashboard;