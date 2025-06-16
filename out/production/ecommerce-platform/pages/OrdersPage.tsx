import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { format, parseISO } from 'date-fns';

interface Order {
  id: number;
  totalAmount: number;
  status: string;
  createdAt?: string;
}

const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const navigate = useNavigate();

  const cancellableStatuses = ['PENDING', 'CONFIRMED', 'SHIPPED'];
  const returnableStatus = 'DELIVERED';
  const ordersPerPage = 5;

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await api.get('/api/order');
            console.log('API response:', response.data);

            // Check the first order's createdAt
            if (response.data.length > 0) {
              console.log('First order createdAt:', response.data[0].createdAt, typeof response.data[0].createdAt);
            }
        setOrders(response.data);
        setTotalPages(Math.ceil(response.data.length / ordersPerPage));
      } catch (error) {
        console.error('Error fetching orders:', error);
      }
    };

    fetchOrders();
  }, []);

  const handleCancel = async (orderId: number) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;

    try {
      await api.patch(`/api/order/${orderId}/cancel`);
      setOrders(prevOrders =>
        prevOrders.map(order =>
          order.id === orderId ? { ...order, status: 'CANCELLED' } : order
        )
      );
    } catch (error) {
      console.error('Error cancelling order:', error);
      alert('Failed to cancel order. It may no longer be cancellable.');
    }
  };

  const handleReturnRequest = async (orderId: number) => {
    const returnReason = prompt('Please specify the reason for return:');
    if (!returnReason) return;

    try {
      await api.post(`/api/order/${orderId}/return`, { reason: returnReason });
      setOrders(prevOrders =>
        prevOrders.map(order =>
          order.id === orderId ? { ...order, status: 'RETURN_REQUESTED' } : order
        )
      );
    } catch (error) {
      console.error('Error requesting return:', error);
      alert('Failed to request return. Only delivered items can be returned.');
    }
  };

  const canCancel = (status: string) => cancellableStatuses.includes(status);
  const canReturn = (status: string) => status === returnableStatus;

  const indexOfLastOrder = currentPage * ordersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
  const currentOrders = orders.slice(indexOfFirstOrder, indexOfLastOrder);

  const paginate = (pageNumber: number) => setCurrentPage(pageNumber);

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return 'Date not available';

  try {
    // Try parsing directly first
    const date = new Date(dateString);

    // If invalid, try cleaning the string
    if (isNaN(date.getTime())) {
      // Remove any unexpected characters or timezone info
      const cleanedDateString = dateString.replace(/\.\d+/, ''); // Remove milliseconds
      const adjustedDate = new Date(cleanedDateString);

      if (!isNaN(adjustedDate.getTime())) {
        const options: Intl.DateTimeFormatOptions = {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        };
        return adjustedDate.toLocaleDateString(undefined, options);
      }

      return dateString; // Fallback to raw string if parsing fails
    }

    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    return date.toLocaleDateString(undefined, options);
  } catch (e) {
    console.warn('Date formatting error for value:', dateString, e);
    return dateString; // Return raw string if parsing fails
  }
};

  const getStatusColor = (status: string) => {
    switch(status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800';
      case 'SHIPPED': return 'bg-green-100 text-green-800';
      case 'DELIVERED': return 'bg-teal-100 text-teal-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'RETURN_REQUESTED': return 'bg-yellow-100 text-yellow-800';
      case 'RETURNED': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">My Orders</h2>

      {orders.length === 0 ? (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <p className="text-lg text-gray-600 mb-4">You have no orders yet.</p>
          <button
            onClick={() => navigate('/products')}
            className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
          >
            Start Shopping
          </button>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {currentOrders.map((order) => (
              <div key={order.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
                <div className="flex justify-between items-center p-4 bg-gray-50 border-b">
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(order.status)}`}>
                    {order.status.replace('_', ' ')}
                  </span>
                  <span className="text-sm text-gray-500">
                    {order.createdAt ? formatDate(order.createdAt) : 'Date not available'}
                  </span>
                </div>

                <div className="p-5">
                  <div className="flex justify-between items-center mb-6">
                    <span className="text-gray-600">Total:</span>
                    <span className="text-lg font-semibold text-gray-800">${order.totalAmount.toFixed(2)}</span>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={() => navigate(`/orders/${order.id}`)}
                      className="flex items-center gap-1 px-3 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      Details
                    </button>

                    {canCancel(order.status) && (
                      <button
                        onClick={() => handleCancel(order.id)}
                        className="flex items-center gap-1 px-3 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700 transition-colors"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                        Cancel
                      </button>
                    )}

                    {canReturn(order.status) && (
                      <button
                        onClick={() => handleReturnRequest(order.id)}
                        className="flex items-center gap-1 px-3 py-2 bg-orange-600 text-white text-sm rounded hover:bg-orange-700 transition-colors"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        Return
                      </button>
                    )}

                    {order.status === 'RETURN_REQUESTED' && (
                      <span className="flex items-center gap-1 text-sm text-orange-600 px-3 py-2">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        Return pending
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
                className={`px-4 py-2 border rounded ${currentPage === 1 ? 'text-gray-400 cursor-not-allowed' : 'text-blue-600 hover:bg-gray-50'}`}
              >
                &laquo; Previous
              </button>

              {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`px-4 py-2 border rounded ${currentPage === number ? 'bg-blue-600 text-white' : 'text-blue-600 hover:bg-gray-50'}`}
                >
                  {number}
                </button>
              ))}

              <button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
                className={`px-4 py-2 border rounded ${currentPage === totalPages ? 'text-gray-400 cursor-not-allowed' : 'text-blue-600 hover:bg-gray-50'}`}
              >
                Next &raquo;
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default OrdersPage;