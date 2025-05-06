import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Toast from '../components/Toast';

interface CartItemDto {
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
}

interface CartResponse {
  id: number;
  items: CartItemDto[];
  updatedAt: string;
}

enum PaymentMethod {
  COD = 'COD',
  CREDIT_CARD = 'CREDIT_CARD',
  UPI = 'UPI',
  NET_BANKING = 'NET_BANKING',
}

const CartPage: React.FC = () => {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PaymentMethod.COD);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleCheckout = async () => {
    if (!cart || cart.items.length === 0) {
      setToastMessage('Cart is empty. Please add items before checking out.');
      return;
    }

    try {
      await api.post('/checkout', null, {
        params: { paymentMethod },
      });

      setToastMessage('Order placed successfully!');
      navigate('/order');
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Checkout failed.';
      setToastMessage(msg);
    }
  };

  const fetchCart = async () => {
    try {
      const response = await api.get<CartResponse>('/cart');
      setCart(response.data);
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateQuantity = async (productId: number, newQuantity: number) => {
    if (newQuantity < 1) return;
    try {
      const response = await api.put<CartResponse>(`/cart/items/${productId}`, null, {
        params: { quantity: newQuantity },
      });
      setCart(response.data);
    } catch (error) {
      console.error('Failed to update quantity:', error);
    }
  };

  const removeItem = async (productId: number) => {
    try {
      await api.delete(`/cart/items/${productId}`);
      fetchCart();
    } catch (error) {
      console.error('Failed to remove item:', error);
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/cart');
      fetchCart();
    } catch (error) {
      console.error('Failed to clear cart:', error);
    }
  };

  // Calculate total price dynamically
  const totalPrice = cart?.items.reduce((acc, item) => acc + item.productPrice * item.quantity, 0) ?? 0;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Your Cart</h2>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}

      {cart ? (
        <>
          {cart.items.length === 0 ? (
            <p>Your cart is empty.</p>
          ) : (
            <div>
              <ul>
                {cart.items.map(item => (
                  <li key={item.productId} className="mb-2 flex items-center gap-4">
                    <span className="flex-1">
                      {item.productName} - ₹{item.productPrice}
                    </span>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                        className="px-2 py-1 bg-gray-200 rounded"
                      >
                        -
                      </button>
                      <span>{item.quantity}</span>
                      <button
                        onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                        className="px-2 py-1 bg-gray-200 rounded"
                      >
                        +
                      </button>
                      <button
                        onClick={() => removeItem(item.productId)}
                        className="px-2 py-1 bg-red-500 text-white rounded"
                      >
                        ❌
                      </button>
                    </div>
                  </li>
                ))}
              </ul>

              <div className="mt-4 font-semibold">
                Last updated: {new Date(cart.updatedAt).toLocaleString()}
              </div>

              <div className="mt-2 text-lg font-bold">
                Total Price: ₹{totalPrice.toFixed(2)}
              </div>

              <div className="mt-4 flex gap-4">
                <button
                  onClick={clearCart}
                  className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                >
                  🗑️ Clear Cart
                </button>

                <button
                  onClick={handleCheckout}
                  className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  ✅ Checkout
                </button>
              </div>
            </div>
          )}
        </>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
};

export default CartPage;