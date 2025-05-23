import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

interface Order {
  id: number;
  totalAmount: number;
  status: string;
}

const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await api.get('/api/order');
        setOrders(response.data);
      } catch (error) {
        console.error('Error fetching orders:', error);
      }
    };

    fetchOrders();
  }, []);

  const handleCancel = async (orderId: number) => {
    try {
      await api.patch(`/api/order/${orderId}/cancel`);
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId ? { ...order, status: 'CANCELLED' } : order
        )
      );
    } catch (error) {
      console.error('Error cancelling order:', error);
    }
  };

  return (
    <div>
      <h2>My Orders</h2>
      <ul>
        {orders.map((order) => (
          <li key={order.id}>
            <p>Total Amount: {order.totalAmount}</p>
            <p>Status: {order.status}</p>
            <button onClick={() => navigate(`/orders/${order.id}`)}>
              View Details
            </button>
            {order.status !== 'CANCELLED' && (
              <button onClick={() => handleCancel(order.id)}>Cancel</button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default OrdersPage;