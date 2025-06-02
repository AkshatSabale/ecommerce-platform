import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';

interface OrderItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
  totalPrice: number;
  productName?: string; // Added for potential future use
  productImage?: string; // Added for potential future use
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
  totalAmount: number;
  status: string;
  paymentMethod: string;
  addressDto: Address;
  list: OrderItem[];
  createdAt?: string;
}

const OrderDetailsPage: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const response = await api.get(`api/order/${orderId}`);
        setOrder(response.data);
      } catch (err: any) {
        setError('Failed to load order details. Please try again later.');
        console.error('Error fetching order details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  const getStatusColor = (status: string) => {
    switch(status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800';
      case 'confirmed': return 'bg-blue-100 text-blue-800';
      case 'shipped': return 'bg-purple-100 text-purple-800';
      case 'delivered': return 'bg-green-100 text-green-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
    </div>
  );

  if (error) return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-red-50 border-l-4 border-red-500 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    </div>
  );

  if (!order) return (
    <div className="max-w-4xl mx-auto p-6 text-center">
      <div className="bg-blue-50 border-l-4 border-blue-500 p-4">
        <p className="text-blue-700">No order found with this ID.</p>
      </div>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-md">
      {/* Order Summary Section */}
      <div className="border-b pb-6 mb-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h2 className="text-2xl font-bold text-gray-800">Order Details</h2>
            {order.createdAt && (
              <p className="text-gray-500 text-sm mt-1">
                Ordered on {formatDate(order.createdAt)}
              </p>
            )}
          </div>
          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(order.status)}`}>
            {order.status}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-gray-50 p-4 rounded-lg">
            <h3 className="font-medium text-gray-700 mb-2">Payment Information</h3>
            <p className="text-gray-600">
              <span className="font-medium">Total:</span> ₹{order.totalAmount.toFixed(2)}
            </p>
            <p className="text-gray-600">
              <span className="font-medium">Method:</span> {order.paymentMethod}
            </p>
          </div>
        </div>
      </div>

      {/* Shipping Address Section */}
      <div className="border-b pb-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-3">Shipping Address</h3>
        <div className="bg-gray-50 p-4 rounded-lg">
          <p className="text-gray-700">
            {order.addressDto.doorNumber}, {order.addressDto.addressLine1}
          </p>
          {order.addressDto.addressLine2 && (
            <p className="text-gray-700">{order.addressDto.addressLine2}</p>
          )}
          <p className="text-gray-700">
            {order.addressDto.city} - {order.addressDto.pinCode}
          </p>
        </div>
      </div>

      {/* Order Items Section */}
      <div>
        <h3 className="text-lg font-semibold text-gray-800 mb-3">Order Items</h3>
        <div className="space-y-4">
          {order.list.map((item) => (
            <div key={item.id} className="flex items-start border-b pb-4 last:border-0">
              {/* Placeholder for product image - replace with actual image if available */}
              <div className="flex-shrink-0 h-16 w-16 bg-gray-200 rounded-md flex items-center justify-center text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>

              <div className="ml-4 flex-1">
                <div className="flex justify-between">
                  <div>
                    <h4 className="text-gray-800 font-medium">Product Name</h4>
                    <p className="text-gray-500 text-sm">Qty: {item.quantity}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-gray-800 font-medium">₹{item.price.toFixed(2)}</p>
                    <p className="text-gray-500 text-sm">Total: ₹{item.totalPrice.toFixed(2)}</p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Order Total Summary */}
        <div className="mt-6 bg-gray-50 p-4 rounded-lg">
          <div className="flex justify-between border-b pb-2 mb-2">
            <span className="text-gray-600">Subtotal</span>
            <span className="text-gray-800">₹{order.totalAmount.toFixed(2)}</span>
          </div>
          <div className="flex justify-between border-b pb-2 mb-2">
            <span className="text-gray-600">Shipping</span>
            <span className="text-gray-800">Free</span>
          </div>
          <div className="flex justify-between font-semibold text-lg mt-2">
            <span>Total</span>
            <span>₹{order.totalAmount.toFixed(2)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetailsPage;