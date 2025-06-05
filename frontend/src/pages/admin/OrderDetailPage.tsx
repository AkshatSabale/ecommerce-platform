// src/pages/admin/OrderDetailPage.tsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { toast } from 'react-toastify';

interface OrderItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
  totalPrice: number;
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

const OrderDetailPage = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/api/order/${orderId}`, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        setOrder(response.data);
      } catch (error) {
        toast.error('Failed to fetch order details');
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!order) {
    return <div className="text-center py-10">Order not found</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <button
        onClick={() => navigate(-1)}
        className="mb-4 px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
      >
        Back to Orders
      </button>

      <h1 className="text-2xl font-bold mb-6">Order Details - #{order.id}</h1>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h2 className="text-xl font-semibold mb-4">Order Information</h2>
            <div className="space-y-2">
              <p><span className="font-medium">Status:</span>
                <span className={`ml-2 px-2 py-1 rounded-full text-xs ${
                  order.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                  order.status === 'CONFIRMED' ? 'bg-blue-100 text-blue-800' :
                  order.status === 'SHIPPED' ? 'bg-green-100 text-green-800' :
                  order.status === 'DELIVERED' ? 'bg-purple-100 text-purple-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {order.status}
                </span>
              </p>
              <p><span className="font-medium">Date:</span> {order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}</p>
              <p><span className="font-medium">Total Amount:</span> ${order.totalAmount.toFixed(2)}</p>
              <p><span className="font-medium">Payment Method:</span> {order.paymentMethod}</p>
            </div>
          </div>

          <div>
            <h2 className="text-xl font-semibold mb-4">Shipping Address</h2>
            <div className="space-y-2">
              <p>{order.addressDto.doorNumber}, {order.addressDto.addressLine1}</p>
              {order.addressDto.addressLine2 && <p>{order.addressDto.addressLine2}</p>}
              <p>{order.addressDto.city}, {order.addressDto.pinCode}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Order Items</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="border-b">
                <th className="text-left py-2 px-4">Product ID</th>
                <th className="text-left py-2 px-4">Quantity</th>
                <th className="text-left py-2 px-4">Price</th>
                <th className="text-left py-2 px-4">Total</th>
              </tr>
            </thead>
            <tbody>
              {order.list.map((item: OrderItem) => (
                <tr key={item.id} className="border-b">
                  <td className="py-2 px-4">{item.productId}</td>
                  <td className="py-2 px-4">{item.quantity}</td>
                  <td className="py-2 px-4">${item.price.toFixed(2)}</td>
                  <td className="py-2 px-4">${item.totalPrice.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default OrderDetailPage;