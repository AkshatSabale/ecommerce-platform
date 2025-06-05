// src/pages/admin/AdminOrdersPage.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { toast } from 'react-toastify';

interface OrderItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
  totalPrice: number;
  productName?: string;
  productImage?: string;
}

interface Address {
  doorNumber: string;
  addressLine1: string;
  addressLine2: string;
  pinCode: number;
  city: string;
}

interface Order {
  id: number;
  userId: number;
  totalAmount: number;
  status: string;
  paymentMethod: string;
  addressDto: Address;
  list: OrderItem[];
  createdAt?: string;
}

const AdminOrdersPage = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, [statusFilter, searchQuery]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/order/admin/orders', {
        params: {
          status: statusFilter || undefined,
          search: searchQuery || undefined
        },
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      setOrders(response.data);
    } catch (error) {
      toast.error('Failed to fetch orders');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const updateOrderStatus = async (orderId: number, newStatus: string) => {
    try {
      let endpoint = '';
      switch (newStatus) {
        case 'CONFIRMED':
          endpoint = 'confirm';
          break;
        case 'SHIPPED':
          endpoint = 'ship';
          break;
        case 'DELIVERED':
          endpoint = 'deliver';
          break;
        default:
          return;
      }

      await api.post(`/api/order/${orderId}/${endpoint}`, {}, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      toast.success(`Order status updated to ${newStatus}`);
      fetchOrders();
    } catch (error: any) {
      toast.error(`Failed to update order status: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleApproveReturn = async (orderId: number) => {
    try {
      await api.post(`/api/order/${orderId}/approve-return`, {}, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      toast.success('Return approved successfully');
      fetchOrders();
    } catch (error: any) {
      toast.error(`Failed to approve return: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleCompleteReturn = async (orderId: number) => {
    try {
      await api.post(`/api/order/${orderId}/complete-return`, {}, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      toast.success('Return completed successfully');
      fetchOrders();
    } catch (error: any) {
      toast.error(`Failed to complete return: ${error.response?.data?.message || error.message}`);
    }
  };

  const getStatusActions = (currentStatus: string, orderId: number) => {
    switch (currentStatus) {
      case 'PENDING':
        return (
          <button
            onClick={() => updateOrderStatus(orderId, 'CONFIRMED')}
            className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Confirm
          </button>
        );
      case 'CONFIRMED':
        return (
          <button
            onClick={() => updateOrderStatus(orderId, 'SHIPPED')}
            className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600"
          >
            Ship
          </button>
        );
      case 'SHIPPED':
        return (
          <button
            onClick={() => updateOrderStatus(orderId, 'DELIVERED')}
            className="px-3 py-1 bg-purple-500 text-white rounded hover:bg-purple-600"
          >
            Deliver
          </button>
        );
      case 'RETURN_REQUESTED':
        return (
          <button
            onClick={() => handleApproveReturn(orderId)}
            className="px-3 py-1 bg-orange-500 text-white rounded hover:bg-orange-600"
          >
            Approve Return
          </button>
        );
      case 'RETURN_APPROVED':
        return (
          <button
            onClick={() => handleCompleteReturn(orderId)}
            className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600"
          >
            Complete Return
          </button>
        );
      default:
        return <span className="text-gray-500">No actions available</span>;
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-6">Order Management</h1>

      <div className="mb-4 flex space-x-4">
        <div>
          <label className="mr-2">Filter by Status:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="p-2 border rounded"
          >
            <option value="">All Orders</option>
            <option value="PENDING">Pending</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="RETURN_REQUESTED">Return Requested</option>
            <option value="RETURN_APPROVED">Return Approved</option>
            <option value="RETURNED">Returned</option>
          </select>
        </div>
        <div>
          <label className="mr-2">Search:</label>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && fetchOrders()}
            placeholder="Order ID or User ID"
            className="p-2 border rounded"
          />
          <button
            onClick={fetchOrders}
            className="ml-2 px-3 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Search
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border">
            <thead>
              <tr>
                <th className="py-2 px-4 border">Order ID</th>
                <th className="py-2 px-4 border">Customer ID</th>
                <th className="py-2 px-4 border">Status</th>
                <th className="py-2 px-4 border">Total Amount</th>
                <th className="py-2 px-4 border">Date</th>
                <th className="py-2 px-4 border">Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="py-2 px-4 border">{order.id}</td>
                  <td className="py-2 px-4 border">{order.userId}</td>
                  <td className="py-2 px-4 border">
                    <span className={`px-2 py-1 rounded-full text-xs ${
                      order.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                      order.status === 'CONFIRMED' ? 'bg-blue-100 text-blue-800' :
                      order.status === 'SHIPPED' ? 'bg-green-100 text-green-800' :
                      order.status === 'DELIVERED' ? 'bg-purple-100 text-purple-800' :
                      order.status === 'RETURN_REQUESTED' ? 'bg-orange-100 text-orange-800' :
                      order.status === 'RETURN_APPROVED' ? 'bg-red-100 text-red-800' :
                      order.status === 'RETURNED' ? 'bg-gray-100 text-gray-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {order.status}
                    </span>
                  </td>
                  <td className="py-2 px-4 border">${order.totalAmount.toFixed(2)}</td>
                  <td className="py-2 px-4 border">
                    {order.createdAt ? new Date(order.createdAt).toLocaleDateString() : 'N/A'}
                  </td>
                  <td className="py-2 px-4 border">
                    <div className="flex space-x-2">
                      {getStatusActions(order.status, order.id)}
                      <button
                        onClick={() => navigate(`/admin/orders/${order.id}`)}
                        className="px-3 py-1 bg-gray-500 text-white rounded hover:bg-gray-600"
                      >
                        View
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminOrdersPage;