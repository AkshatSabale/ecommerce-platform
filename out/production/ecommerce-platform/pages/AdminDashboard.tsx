import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Toast from '../components/Toast';

interface Product {
  id?: number;
  name: string;
  price: number;
  quantity: number;
  description: string;
  imageFilename: string;
}

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [newProduct, setNewProduct] = useState<Product>({
    name: '',
    price: 0,
    quantity: 0,
    description: '',
    imageFilename: ''
  });
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleCreateProduct = async () => {
    try {
      setLoading(true);
      await api.post('/api/products', newProduct);
      setToastMessage('Product created successfully!');
      setNewProduct({
        name: '',
        price: 0,
        quantity: 0,
        description: '',
        imageFilename: ''
      });
      // Refresh products list
      fetchProducts();
    } catch (error) {
      setToastMessage('Failed to create product');
      console.error('Error creating product:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProduct = async () => {
    if (!editingProduct?.id) return;

    try {
      setLoading(true);
      await api.put(`/api/products/${editingProduct.id}`, editingProduct);
      setToastMessage('Product updated successfully!');
      setEditingProduct(null);
      // Refresh products list
      fetchProducts();
    } catch (error) {
      setToastMessage('Failed to update product');
      console.error('Error updating product:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (id: number) => {
    try {
      setLoading(true);
      await api.delete(`/api/products/${id}`);
      setToastMessage('Product deleted successfully!');
      // Refresh products list
      fetchProducts();
    } catch (error) {
      setToastMessage('Failed to delete product');
      console.error('Error deleting product:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await api.get<Product[]>('/api/products');
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  // Initial fetch
  React.useEffect(() => {
    fetchProducts();
  }, []);

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-8">Admin Dashboard</h1>

      {/* Create Product Section */}
      <div className="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 className="text-xl font-semibold mb-4">
          {editingProduct ? 'Edit Product' : 'Create New Product'}
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              type="text"
              className="w-full p-2 border rounded"
              value={editingProduct ? editingProduct.name : newProduct.name}
              onChange={(e) => editingProduct
                ? setEditingProduct({...editingProduct, name: e.target.value})
                : setNewProduct({...newProduct, name: e.target.value})}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Price</label>
            <input
              type="number"
              className="w-full p-2 border rounded"
              value={editingProduct ? editingProduct.price : newProduct.price}
              onChange={(e) => editingProduct
                ? setEditingProduct({...editingProduct, price: parseFloat(e.target.value)})
                : setNewProduct({...newProduct, price: parseFloat(e.target.value)})}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
            <input
              type="number"
              className="w-full p-2 border rounded"
              value={editingProduct ? editingProduct.quantity : newProduct.quantity}
              onChange={(e) => editingProduct
                ? setEditingProduct({...editingProduct, quantity: parseInt(e.target.value)})
                : setNewProduct({...newProduct, quantity: parseInt(e.target.value)})}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Image Filename</label>
            <input
              type="text"
              className="w-full p-2 border rounded"
              value={editingProduct ? editingProduct.imageFilename : newProduct.imageFilename}
              onChange={(e) => editingProduct
                ? setEditingProduct({...editingProduct, imageFilename: e.target.value})
                : setNewProduct({...newProduct, imageFilename: e.target.value})}
            />
          </div>

          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              className="w-full p-2 border rounded"
              rows={3}
              value={editingProduct ? editingProduct.description : newProduct.description}
              onChange={(e) => editingProduct
                ? setEditingProduct({...editingProduct, description: e.target.value})
                : setNewProduct({...newProduct, description: e.target.value})}
            />
          </div>
        </div>

        <div className="mt-4">
          {editingProduct ? (
            <>
              <button
                onClick={handleUpdateProduct}
                disabled={loading}
                className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
              >
                {loading ? 'Updating...' : 'Update Product'}
              </button>
              <button
                onClick={() => setEditingProduct(null)}
                className="bg-gray-500 text-white px-4 py-2 rounded"
              >
                Cancel
              </button>
            </>
          ) : (
            <button
              onClick={handleCreateProduct}
              disabled={loading}
              className="bg-green-500 text-white px-4 py-2 rounded"
            >
              {loading ? 'Creating...' : 'Create Product'}
            </button>
          )}
        </div>
      </div>

      {/* Products List Section */}
      <div className="bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-semibold mb-4">Manage Products</h2>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Quantity</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {products.map((product) => (
                <tr key={product.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{product.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{product.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${product.price}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{product.quantity}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => setEditingProduct(product)}
                      className="text-blue-600 hover:text-blue-900 mr-3"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => product.id && handleDeleteProduct(product.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {toastMessage && (
        <Toast
          message={toastMessage}
          onClose={() => setToastMessage(null)}
        />
      )}
    </div>
  );
};

export default AdminDashboard;