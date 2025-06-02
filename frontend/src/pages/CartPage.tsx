import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Toast from '../components/Toast';
import { FiTrash2, FiPlus, FiMinus, FiShoppingCart, FiArrowLeft } from 'react-icons/fi';

interface CartItemDto {
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  imageUrl?: string; // Added for product images
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
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  const handleCheckout = () => {
    if (!cart || cart.items.length === 0) {
      setToastMessage('Cart is empty. Please add items before checking out.');
      return;
    }
    navigate('/order', { state: { cart, paymentMethod } });
  };

  const fetchCart = async () => {
    setIsLoading(true);
    try {
      const response = await api.get<CartResponse>('/cart');
      setCart(response.data);
    } catch (error) {
      console.error('Failed to fetch cart:', error);
      setToastMessage('Failed to load your cart');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { fetchCart(); }, []);

  const updateQuantity = async (productId: number, newQuantity: number) => {
    if (newQuantity < 1) return;
    try {
      const response = await api.put<CartResponse>(`/cart/items/${productId}`, null, {
        params: { quantity: newQuantity },
      });
      setCart(response.data);
    } catch (error) {
      console.error('Failed to update quantity:', error);
      setToastMessage('Failed to update quantity');
    }
  };

  const removeItem = async (productId: number) => {
    try {
      await api.delete(`/cart/items/${productId}`);
      fetchCart();
      setToastMessage('Item removed from cart');
    } catch (error) {
      console.error('Failed to remove item:', error);
      setToastMessage('Failed to remove item');
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/cart');
      fetchCart();
      setToastMessage('Cart cleared successfully');
    } catch (error) {
      console.error('Failed to clear cart:', error);
      setToastMessage('Failed to clear cart');
    }
  };

  const totalPrice = cart?.items.reduce((acc, item) => acc + item.productPrice * item.quantity, 0) ?? 0;
  const totalItems = cart?.items.reduce((acc, item) => acc + item.quantity, 0) ?? 0;

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center text-blue-600 hover:text-blue-800 mb-6"
      >
        <FiArrowLeft className="mr-2" /> Continue Shopping
      </button>

      <h1 className="text-3xl font-bold mb-8 flex items-center">
        <FiShoppingCart className="mr-3" /> Your Shopping Cart
      </h1>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}

      {isLoading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      ) : cart ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2">
            {cart.items.length === 0 ? (
              <div className="bg-white rounded-lg shadow p-8 text-center">
                <FiShoppingCart className="mx-auto text-5xl text-gray-300 mb-4" />
                <h3 className="text-xl font-semibold mb-2">Your cart is empty</h3>
                <p className="text-gray-600 mb-4">Looks like you haven't added any items yet</p>
                <button
                  onClick={() => navigate('/')}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  Browse Products
                </button>
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow overflow-hidden">
                <div className="divide-y divide-gray-200">
                  {cart.items.map(item => (
                    <div key={item.productId} className="p-4 flex items-start sm:items-center gap-4">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.productName}
                          className="w-20 h-20 object-cover rounded"
                        />
                      ) : (
                        <div className="w-20 h-20 bg-gray-100 rounded flex items-center justify-center">
                          <FiShoppingCart className="text-gray-400 text-xl" />
                        </div>
                      )}

                      <div className="flex-1">
                        <h3 className="font-medium text-gray-900">{item.productName}</h3>
                        <p className="text-gray-600">₹{item.productPrice.toFixed(2)}</p>
                      </div>

                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                          className="p-1 rounded-full bg-gray-100 hover:bg-gray-200 transition"
                          disabled={item.quantity <= 1}
                        >
                          <FiMinus className="text-gray-600" />
                        </button>

                        <span className="w-8 text-center">{item.quantity}</span>

                        <button
                          onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                          className="p-1 rounded-full bg-gray-100 hover:bg-gray-200 transition"
                        >
                          <FiPlus className="text-gray-600" />
                        </button>

                        <button
                          onClick={() => removeItem(item.productId)}
                          className="p-2 text-red-500 hover:text-red-700 transition"
                          title="Remove item"
                        >
                          <FiTrash2 />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="p-4 border-t border-gray-200 flex justify-end">
                  <button
                    onClick={clearCart}
                    className="flex items-center text-red-600 hover:text-red-800 transition"
                  >
                    <FiTrash2 className="mr-2" /> Clear Cart
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Order Summary */}
          <div className="bg-white rounded-lg shadow p-6 h-fit sticky top-4">
            <h3 className="text-lg font-semibold mb-4">Order Summary</h3>

            <div className="space-y-3 mb-6">
              <div className="flex justify-between">
                <span className="text-gray-600">Subtotal ({totalItems} items)</span>
                <span>₹{totalPrice.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Shipping</span>
                <span className="text-green-600">FREE</span>
              </div>
              <div className="flex justify-between border-t border-gray-200 pt-3">
                <span className="font-medium">Total</span>
                <span className="font-bold">₹{totalPrice.toFixed(2)}</span>
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">Payment Method</label>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value as PaymentMethod)}
                className="w-full p-2 border border-gray-300 rounded focus:ring-blue-500 focus:border-blue-500"
              >
                <option value={PaymentMethod.COD}>Cash on Delivery</option>
                <option value={PaymentMethod.CREDIT_CARD}>Credit Card</option>
                <option value={PaymentMethod.UPI}>UPI</option>
                <option value={PaymentMethod.NET_BANKING}>Net Banking</option>
              </select>
            </div>

            <button
              onClick={handleCheckout}
              disabled={!cart || cart.items.length === 0}
              className={`w-full py-3 px-4 rounded-lg font-medium text-white transition
                ${!cart || cart.items.length === 0
                  ? 'bg-gray-400 cursor-not-allowed'
                  : 'bg-green-600 hover:bg-green-700'}`}
            >
              Proceed to Checkout
            </button>

            <p className="text-xs text-gray-500 mt-4 text-center">
              Last updated: {new Date(cart.updatedAt).toLocaleString()}
            </p>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <h3 className="text-xl font-semibold mb-2">Unable to load your cart</h3>
          <button
            onClick={fetchCart}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition mt-4"
          >
            Try Again
          </button>
        </div>
      )}
    </div>
  );
};

export default CartPage;