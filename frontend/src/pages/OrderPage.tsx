import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

interface AddressData {
  doorNumber: string;
  addressLine1: string;
  addressLine2: string;
  pinCode: number;
  city: string;
}

interface LocationState {
  cart: {
    items: Array<{
      productId: number;
      productName: string;
      productPrice: number;
      quantity: number;
    }>;
  };
}

interface RazorpayOrderResponse {
  id: string;
  amount: number;
  currency: string;
  receipt: string;
  status: string;
}

interface UserDetails {
  id: number;
  username: string;
  email: string;
  phone?: string;
  name?: string;
}

const OrderPage: React.FC = () => {
  const { state } = useLocation() as { state: LocationState | null };
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [paymentMethod, setPaymentMethod] = useState<'cod' | 'online'>('cod');
  const [useSavedAddress, setUseSavedAddress] = useState(true);
  const [address, setAddress] = useState<AddressData>({
    doorNumber: '',
    addressLine1: '',
    addressLine2: '',
    pinCode: 0,
    city: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [userDetails, setUserDetails] = useState<UserDetails | null>(null);

  // Calculate total price safely
  const totalPrice = state?.cart?.items.reduce(
    (sum: number, item) => sum + item.productPrice * item.quantity,
    0
  ) || 0;

  useEffect(() => {
    const fetchAddress = async () => {
      try {
        const addressResponse = await api.get<AddressData>('/api/address');
        setAddress(addressResponse.data);
      } catch (err) {
        console.error('Failed to load address:', err);
        setError('Failed to load address');
      }
    };

    if (useSavedAddress) {
      fetchAddress();
    } else {
      // Clear address fields when checkbox is unticked
      setAddress({
        doorNumber: '',
        addressLine1: '',
        addressLine2: '',
        pinCode: 0,
        city: '',
      });
    }
  }, [useSavedAddress]);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    if (!state?.cart?.items) {
      navigate('/cart');
      return;
    }

    const fetchUser = async () => {
      try {
        const userResponse = await api.get<UserDetails>('/users/me');
        setUserDetails(userResponse.data);
      } catch (err) {
        console.error('Failed to load user data:', err);
        setError('Failed to load user data');
      }
    };

    fetchUser();
  }, [isAuthenticated, navigate, state]);

  const handleInputChange = (field: keyof AddressData, value: string) => {
    setAddress(prev => ({ ...prev, [field]: value }));
  };

  const validateAddress = (): boolean => {
    if (!address.doorNumber || !address.addressLine1 || !address.city || !address.pinCode) {
      setError('Please fill all required address fields');
      return false;
    }
    if (address.pinCode.toString().length !== 6) {
      setError('Pin code must be 6 digits');
      return false;
    }
    setError('');
    return true;
  };

  const handlePlaceOrder = async (paymentId: string | null, razorpayOrderId: string | null) => {
    try {
      const response = await api.post<{
        id: number;
        list: any[];
        status: string;
        totalAmount: number;
        paymentMethod: string;
        addressDto: any;
        createdAt: string;
      }>('/checkout', {
        paymentMethod: paymentMethod === 'cod' ? 'COD' : 'UPI',
        address,
        paymentId,
        orderId: razorpayOrderId,
      });

      navigate(`/orders/${response.data.id}`);
    } catch (err) {
      console.error('Checkout failed:', err);
      setError('Failed to place order. Please try again.');
    }
  };

  const loadRazorpayScript = (): Promise<boolean> => {
    return new Promise(resolve => {
      if ((window as any).Razorpay) {
        resolve(true);
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  const handleOnlinePayment = async () => {
    if (!validateAddress() || !userDetails) return;

    setLoading(true);
    try {
      const scriptLoaded = await loadRazorpayScript();
      if (!scriptLoaded) {
        setError('Payment gateway failed to load');
        return;
      }

      const razorpayOrder = await api.post<RazorpayOrderResponse>('/payments', null, {
        params: { amount: Math.round(totalPrice * 100), currency: 'INR' }
      }).then(res => res.data);

      const options = {
        key: process.env.REACT_APP_RAZORPAY_KEY_ID || 'rzp_test_KzYYpa28iwjEsD',
        amount: razorpayOrder.amount.toString(),
        currency: razorpayOrder.currency,
        name: 'Ecommerce Platform',
        description: 'Order Payment',
        order_id: razorpayOrder.id,
        handler: async function (response: any) {
          try {
            const verificationRes = await api.post<boolean>('/payments/verify', null, {
              params: {
                orderId: response.razorpay_order_id,
                paymentId: response.razorpay_payment_id,
                signature: response.razorpay_signature,
              },
            });

            if (verificationRes.data) {
              await handlePlaceOrder(response.razorpay_payment_id, response.razorpay_order_id);
            } else {
              setError('Payment verification failed');
            }
          } catch (err) {
            console.error('Verification failed:', err);
            setError('Payment verification error');
          }
        },
        prefill: {
          name: userDetails.name || userDetails.username,
          email: userDetails.email,
          contact: userDetails.phone || '',
        },
        notes: {
          address: JSON.stringify(address),
          userId: userDetails.id.toString(),
        },
        theme: { color: '#4f46e5' },
        modal: {
          ondismiss: () => {
            setLoading(false);
            setError('Payment was cancelled');
          },
        },
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.open();
    } catch (err: any) {
      console.error('Payment initialization error:', err);
      setError(err.response?.data?.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  if (!state?.cart?.items) {
    return (
      <div className="max-w-2xl mx-auto p-4 md:p-6">
        <div className="bg-white rounded-lg shadow-md p-6 text-center">
          <h2 className="text-2xl font-bold mb-4">No Order Items Found</h2>
          <p className="mb-4">Your cart appears to be empty or the session expired.</p>
          <button
            onClick={() => navigate('/cart')}
            className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
          >
            Back to Cart
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-4 md:p-6">
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-6">Complete Your Order</h2>

        {/* Order Summary */}
        <div className="mb-8">
          <h3 className="text-lg font-semibold mb-3">Order Summary</h3>
          {state.cart.items.map((item) => (
            <div key={item.productId} className="flex justify-between py-2 border-b">
              <span>{item.productName} × {item.quantity}</span>
              <span>₹{(item.productPrice * item.quantity).toFixed(2)}</span>
            </div>
          ))}
          <div className="flex justify-between font-bold mt-4">
            <span>Total</span>
            <span>₹{totalPrice.toFixed(2)}</span>
          </div>
        </div>

        {/* Address Section */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold">Shipping Address</h3>
            <label className="flex items-center text-sm">
              <input
                type="checkbox"
                checked={useSavedAddress}
                onChange={() => setUseSavedAddress(prev => !prev)}
                disabled={loading}
              />
              <span className="ml-2">Use saved address</span>
            </label>
          </div>

          {error && <div className="text-red-600 text-sm mb-4">{error}</div>}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input disabled={useSavedAddress || loading} placeholder="Door Number*" value={address.doorNumber} onChange={e => handleInputChange('doorNumber', e.target.value)} className="p-2 border rounded" />
            <input disabled={useSavedAddress || loading} type="number" placeholder="Pin Code*" value={address.pinCode || ''} onChange={e => handleInputChange('pinCode', e.target.value)} className="p-2 border rounded" />
            <input disabled={useSavedAddress || loading} placeholder="Address Line 1*" value={address.addressLine1} onChange={e => handleInputChange('addressLine1', e.target.value)} className="col-span-2 p-2 border rounded" />
            <input disabled={useSavedAddress || loading} placeholder="Address Line 2" value={address.addressLine2} onChange={e => handleInputChange('addressLine2', e.target.value)} className="col-span-2 p-2 border rounded" />
            <input disabled={useSavedAddress || loading} placeholder="City*" value={address.city} onChange={e => handleInputChange('city', e.target.value)} className="col-span-2 p-2 border rounded" />
          </div>
        </div>

        {/* Payment Method */}
        <div className="mb-8">
          <h3 className="text-lg font-semibold mb-3">Payment Method</h3>
          <label className="flex items-center gap-3 mb-2">
            <input type="radio" value="cod" checked={paymentMethod === 'cod'} onChange={() => setPaymentMethod('cod')} />
            Cash on Delivery
          </label>
          <label className="flex items-center gap-3">
            <input type="radio" value="online" checked={paymentMethod === 'online'} onChange={() => setPaymentMethod('online')} />
            Online Payment (UPI / Card)
          </label>
        </div>

        {/* Submit Button */}
        <button
          disabled={loading}
          onClick={() => {
            if (paymentMethod === 'cod') {
              if (validateAddress()) {
                setLoading(true);
                handlePlaceOrder(null, null).finally(() => setLoading(false));
              }
            } else {
              handleOnlinePayment();
            }
          }}
          className={`w-full py-3 rounded text-white font-medium ${loading ? 'bg-indigo-400' : 'bg-indigo-600 hover:bg-indigo-700'}`}
        >
          {loading ? 'Processing...' : paymentMethod === 'cod' ? 'Place Order' : 'Proceed to Payment'}
        </button>
      </div>
    </div>
  );
};

export default OrderPage;