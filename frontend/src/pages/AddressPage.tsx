import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Toast from '../components/Toast';
import { FaHome, FaEdit, FaTrash, FaSave, FaMapMarkerAlt } from 'react-icons/fa';

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
  const [loading, setLoading] = useState(false);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setAddress(prev => ({ ...prev, [name]: name === 'pinCode' ? Number(value) : value }));
  };

  const fetchAddress = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/address');
      setAddress(res.data);
      setHasAddress(true);
    } catch (err: any) {
      if (err.response?.status === 401) {
        showToast('Please log in to manage your address.');
      } else if (err.response?.status === 404) {
        setHasAddress(false);
      } else {
        showToast('Something went wrong while fetching address.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      await api.post('/api/address', address);
      setHasAddress(true);
      showToast('Address saved successfully!');
    } catch (err: any) {
      if (err.response?.status === 401) {
        showToast('Please log in.');
      } else {
        showToast(err.response?.data || 'Failed to save address.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    setLoading(true);
    try {
      await api.put('/api/address', address);
      showToast('Address updated successfully!');
    } catch (err: any) {
      showToast(err.response?.data || 'Failed to update address.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    setLoading(true);
    try {
      await api.delete('/api/address');
      setAddress({ doorNumber: '', addressLine1: '', addressLine2: '', pinCode: 0, city: '' });
      setHasAddress(false);
      showToast('Address deleted.');
    } catch (err: any) {
      showToast(err.response?.data || 'Failed to delete address.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAddress();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900">
            <FaMapMarkerAlt className="inline-block mr-2 text-blue-500" />
            Delivery Address
          </h1>
          <p className="mt-2 text-sm text-gray-600">
            {hasAddress ? 'Manage your delivery address' : 'Add your delivery address to continue'}
          </p>
        </div>

        {loading && !hasAddress ? (
          <div className="bg-white shadow rounded-lg p-6 text-center">
            <div className="animate-pulse flex flex-col space-y-4">
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
            </div>
          </div>
        ) : (
          <div className="bg-white shadow overflow-hidden rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                <div className="sm:col-span-2">
                  <label htmlFor="doorNumber" className="block text-sm font-medium text-gray-700">
                    Door/House Number
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="doorNumber"
                      id="doorNumber"
                      value={address.doorNumber}
                      onChange={handleChange}
                      className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                      placeholder="e.g. 123"
                    />
                  </div>
                </div>

                <div className="sm:col-span-4">
                  <label htmlFor="addressLine1" className="block text-sm font-medium text-gray-700">
                    Street Address
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="addressLine1"
                      id="addressLine1"
                      value={address.addressLine1}
                      onChange={handleChange}
                      className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                      placeholder="e.g. Main Street"
                    />
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <label htmlFor="addressLine2" className="block text-sm font-medium text-gray-700">
                    Apartment, Suite, etc. (Optional)
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="addressLine2"
                      id="addressLine2"
                      value={address.addressLine2}
                      onChange={handleChange}
                      className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                      placeholder="e.g. Apt 4B"
                    />
                  </div>
                </div>

                <div className="sm:col-span-3">
                  <label htmlFor="city" className="block text-sm font-medium text-gray-700">
                    City
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="city"
                      id="city"
                      value={address.city}
                      onChange={handleChange}
                      className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                      placeholder="e.g. New York"
                    />
                  </div>
                </div>

                <div className="sm:col-span-3">
                  <label htmlFor="pinCode" className="block text-sm font-medium text-gray-700">
                    ZIP/Postal Code
                  </label>
                  <div className="mt-1">
                    <input
                      type="number"
                      name="pinCode"
                      id="pinCode"
                      value={address.pinCode || ''}
                      onChange={handleChange}
                      className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                      placeholder="e.g. 10001"
                    />
                  </div>
                </div>
              </div>
            </div>

            <div className="px-4 py-3 bg-gray-50 text-right sm:px-6 flex justify-between">
              <button
                onClick={handleDelete}
                disabled={!hasAddress || loading}
                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white ${
                  hasAddress
                    ? 'bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500'
                    : 'bg-gray-400 cursor-not-allowed'
                }`}
              >
                <FaTrash className="mr-2" />
                Delete Address
              </button>

              <div className="space-x-3">
                {hasAddress ? (
                  <button
                    onClick={handleUpdate}
                    disabled={loading}
                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    <FaEdit className="mr-2" />
                    {loading ? 'Updating...' : 'Update Address'}
                  </button>
                ) : (
                  <button
                    onClick={handleSave}
                    disabled={loading}
                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                  >
                    <FaSave className="mr-2" />
                    {loading ? 'Saving...' : 'Save Address'}
                  </button>
                )}
              </div>
            </div>
          </div>
        )}

        {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
      </div>
    </div>
  );
};

export default AddressPage;