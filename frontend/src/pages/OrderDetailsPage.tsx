import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api'; // Adjust path if needed

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
  totalAmount: number;
  status: string;
  paymentMethod: string;
  addressDto: Address;
  list: OrderItem[];
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
        setError('Failed to load order details.');
        console.error('Error fetching order details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  if (loading) return <div>Loading order details...</div>;
  if (error) return <div>{error}</div>;
  if (!order) return <div>No order found.</div>;

  return (
    <div>
      <h2>Order Details (#{order.id})</h2>
      <p><strong>Status:</strong> {order.status}</p>
      <p><strong>Total Amount:</strong> ₹{order.totalAmount.toFixed(2)}</p>
      <p><strong>Payment Method:</strong> {order.paymentMethod}</p>

      <h3>Shipping Address</h3>
      <p>
        {order.addressDto.doorNumber}, {order.addressDto.addressLine1},<br />
        {order.addressDto.addressLine2}, {order.addressDto.city} - {order.addressDto.pinCode}
      </p>

      <h3>Items</h3>
      <ul>
        {order.list.map((item) => (
          <li key={item.id} style={{ marginBottom: '1rem' }}>
            <p><strong>Product ID:</strong> {item.productId}</p>
            <p><strong>Quantity:</strong> {item.quantity}</p>
            <p><strong>Price:</strong> ₹{item.price.toFixed(2)}</p>
            <p><strong>Total:</strong> ₹{item.totalPrice.toFixed(2)}</p>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default OrderDetailsPage;