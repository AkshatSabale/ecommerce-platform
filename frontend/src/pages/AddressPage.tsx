import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Toast from '../components/Toast';

interface AddressData {
  doorNumber: string;
  addressLine1: string;
  addressLine2: string;
  pinCode: number;
  city: string;
}

const AddressPage: React.FC = () => {
  const [address, setAddress] = useState<AddressData>({
    doorNumber: '',
    addressLine1: '',
    addressLine2: '',
    pinCode: 0,
    city: '',
  });

  const [hasAddress, setHasAddress] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setAddress(prev => ({ ...prev, [name]: name === 'pinCode' ? Number(value) : value }));
  };

  const fetchAddress = async () => {
    try {
      const res = await api.get('/api/address');
      setAddress(res.data);
      setHasAddress(true);
    } catch (err: any) {
      if (err.response?.status === 401) {
        showToast('Please log in to manage your address.');
      } else if (err.response?.status === 404) {
        setHasAddress(false); // no address
      } else {
        showToast('Something went wrong while fetching address.');
      }
    }
  };

  const handleSave = async () => {
    try {
      await api.post('/api/address', address);
      setHasAddress(true);
      showToast('Address saved successfully.');
    } catch (err: any) {
      if (err.response?.status === 401) {
        showToast('Please log in.');
      } else {
        showToast(err.response?.data || 'Failed to save address.');
      }
    }
  };

  const handleUpdate = async () => {
    try {
      await api.put('/api/address', address);
      showToast('Address updated successfully.');
    } catch (err: any) {
      showToast(err.response?.data || 'Failed to update address.');
    }
  };

  const handleDelete = async () => {
    try {
      await api.delete('/api/address');
      setAddress({ doorNumber: '', addressLine1: '', addressLine2: '', pinCode: 0, city: '' });
      setHasAddress(false);
      showToast('Address deleted.');
    } catch (err: any) {
      showToast(err.response?.data || 'Failed to delete address.');
    }
  };

  useEffect(() => {
    fetchAddress();
  }, []);

  return (
    <div className="max-w-md mx-auto p-6 bg-white shadow rounded mt-8">
      <h2 className="text-xl font-bold mb-4 text-center">Your Delivery Address</h2>
      <div className="space-y-4">
        <input
          type="text"
          name="doorNumber"
          placeholder="Door Number"
          value={address.doorNumber}
          onChange={handleChange}
          className="w-full border border-gray-300 px-3 py-2 rounded"
        />
        <input
          type="text"
          name="addressLine1"
          placeholder="Address Line 1"
          value={address.addressLine1}
          onChange={handleChange}
          className="w-full border border-gray-300 px-3 py-2 rounded"
        />
        <input
          type="text"
          name="addressLine2"
          placeholder="Address Line 2"
          value={address.addressLine2}
          onChange={handleChange}
          className="w-full border border-gray-300 px-3 py-2 rounded"
        />
        <input
          type="text"
          name="city"
          placeholder="City"
          value={address.city}
          onChange={handleChange}
          className="w-full border border-gray-300 px-3 py-2 rounded"
        />
        <input
          type="number"
          name="pinCode"
          placeholder="Pincode"
          value={address.pinCode}
          onChange={handleChange}
          className="w-full border border-gray-300 px-3 py-2 rounded"
        />
      </div>

      <div className="flex justify-between mt-6">
        <button
          onClick={handleSave}
          disabled={hasAddress}
          className={`px-4 py-2 rounded text-white ${
            hasAddress ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-500 hover:bg-green-600'
          }`}
        >
          Save
        </button>

        <button
          onClick={handleUpdate}
          disabled={!hasAddress}
          className={`px-4 py-2 rounded text-white ${
            hasAddress ? 'bg-blue-500 hover:bg-blue-600' : 'bg-gray-400 cursor-not-allowed'
          }`}
        >
          Update
        </button>

        <button
          onClick={handleDelete}
          disabled={!hasAddress}
          className={`px-4 py-2 rounded text-white ${
            hasAddress ? 'bg-red-500 hover:bg-red-600' : 'bg-gray-400 cursor-not-allowed'
          }`}
        >
          Delete
        </button>
      </div>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
    </div>
  );
};

export default AddressPage;