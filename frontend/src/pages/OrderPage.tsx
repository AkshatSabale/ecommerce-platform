import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../services/api';

interface AddressData {
  doorNumber: string;
  addressLine1: string;
  addressLine2: string;
  pinCode: number;
  city: string;
}

interface LocationState {
  cart: any;
}

const OrderPage: React.FC = () => {
  const { state } = useLocation() as { state: LocationState };
  const navigate = useNavigate();
  const [paymentMethod, setPaymentMethod] = useState('cod');
  const [useSavedAddress, setUseSavedAddress] = useState(true);
  const [address, setAddress] = useState<AddressData>({
    doorNumber: '',
    addressLine1: '',
    addressLine2: '',
    pinCode: 0,
    city: '',
  });

  // Fetch address when checkbox is checked
  useEffect(() => {
    if (useSavedAddress) {
      api.get('/api/address')
        .then(response => {
          setAddress(response.data);
        })
        .catch(() => {
          // Handle failure, could also show error message or leave fields empty
        });
    } else {
      // If checkbox is unchecked, clear the address
      setAddress({
        doorNumber: '',
        addressLine1: '',
        addressLine2: '',
        pinCode: 0,
        city: '',
      });
    }
  }, [useSavedAddress]);

  const handleInputChange = (field: keyof AddressData, value: string) => {
    setAddress(prev => ({ ...prev, [field]: value }));
  };

  const handlePlaceOrder = async () => {
    if (paymentMethod === 'cod') {
      try {
        await api.post('/checkout', {
          paymentMethod,
          address,
        });
        navigate('/order-confirmation');
      } catch (error) {
        console.error('Checkout failed:', error);
      }
    }
  };

  const loadRazorpayScript = () => {
    return new Promise((resolve) => {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  const handleOnlinePayment = async () => {
    try {
      const res = await loadRazorpayScript();
          if (!res) {
            alert('Razorpay SDK failed to load. Are you online?');
            return;
          }
      console.log('Pay Now clicked!');
      const totalAmount = state.cart.items.reduce(
        (sum: number, item: any) => sum + item.productPrice * item.quantity,
        0
      );

      const response = await api.post('/payment/create', null, {
        params: { amount: totalAmount },
      });

      const razorpayOrder = response.data;

      const options = {
        key: 'rzp_test_KzYYpa28iwjEsD',
        amount: razorpayOrder.amount,
        currency: razorpayOrder.currency,
        name: 'Your Store',
        description: 'Test Transaction',
        order_id: razorpayOrder.id,
        handler: async function (response: any) {
          const { razorpay_payment_id, razorpay_order_id, razorpay_signature } = response;

          const verificationRes = await api.post('/payment/verify', null, {
            params: {
              orderId: razorpay_order_id,
              paymentId: razorpay_payment_id,
              signature: razorpay_signature,
            },
          });

          if (verificationRes.data === true) {
            await api.post('/checkout', {
              paymentMethod: 'UPI',
              address,
            });
            navigate('/order-confirmation');
          } else {
            alert('Payment verification failed');
          }
        },
        theme: {
          color: '#3399cc',
        },
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.open();
    } catch (err) {
      console.error('Payment error:', err);
      alert('Payment failed. Try again.');
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Order Details</h2>

      {/* Checkbox to toggle saved address */}
      <label className="block mb-4">
        <input
          type="checkbox"
          checked={useSavedAddress}
          onChange={() => setUseSavedAddress(prev => !prev)}
        />
        <span className="ml-2">Use saved address</span>
      </label>

      {/* Always show address fields */}
      <div className="space-y-2 mb-4">
        <input
          placeholder="Door Number"
          value={address.doorNumber}
          onChange={e => handleInputChange('doorNumber', e.target.value)}
          disabled={useSavedAddress}
          className="w-full p-2 border border-gray-300 rounded"
        />
        <input
          placeholder="Address Line 1"
          value={address.addressLine1}
          onChange={e => handleInputChange('addressLine1', e.target.value)}
          disabled={useSavedAddress}
          className="w-full p-2 border border-gray-300 rounded"
        />
        <input
          placeholder="Address Line 2"
          value={address.addressLine2}
          onChange={e => handleInputChange('addressLine2', e.target.value)}
          disabled={useSavedAddress}
          className="w-full p-2 border border-gray-300 rounded"
        />
        <input
          placeholder="Pin Code"
          value={address.pinCode}
          onChange={e => handleInputChange('pinCode', e.target.value)}
          disabled={useSavedAddress}
          className="w-full p-2 border border-gray-300 rounded"
        />
        <input
          placeholder="City"
          value={address.city}
          onChange={e => handleInputChange('city', e.target.value)}
          disabled={useSavedAddress}
          className="w-full p-2 border border-gray-300 rounded"
        />
      </div>

      <div className="mb-4">
        <label className="block mb-2 font-medium">Payment Method</label>
        <select
          value={paymentMethod}
          onChange={(e) => setPaymentMethod(e.target.value)}
          className="p-2 border rounded"
        >
          <option value="cod">Cash on Delivery</option>
          <option value="online">Online Payment</option>
        </select>
      </div>

      {paymentMethod === 'cod' ? (
        <button
          onClick={handlePlaceOrder}
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          🛒 Place Order
        </button>
      ) : (
        <button
          onClick={handleOnlinePayment}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          💳 Pay Now
        </button>
      )}
    </div>
  );
};

export default OrderPage;