import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import api from '../services/api';
import { addToCart } from '../services/cartService';
import Toast from './Toast';

interface Product {
  id: number;
  name: string;
  quantity: number;
  price: number;
  imageFilename: string;
}

const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const location = useLocation();

  const fetchProducts = async (query: string = '') => {
    try {
      const response = query
        ? await api.get<Product[]>(`/products/search?query=${encodeURIComponent(query)}`)
        : await api.get<Product[]>('/products');
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  // Effect runs whenever the URL changes
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const query = params.get('query') || '';
    fetchProducts(query);
  }, [location.search]);

  const handleAddToCart = async (productId: number) => {
    try {
      await addToCart(productId, 1);
      setToastMessage('Item added to cart!');
    } catch (error: any) {
      if (error.response && error.response.status === 403) {
        setToastMessage('You need to log in to add items to your cart.');
      } else {
        setToastMessage('Something went wrong. Please try again.');
      }
    }
  };

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold mb-4">Products</h1>

      <ul className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {products.map((product) => (
          <li key={product.id} className="border p-4 rounded">
            <h2 className="text-xl font-semibold">{product.name}</h2>
            <p className="mb-1">
              Quantity: <span className="font-medium">{product.quantity}</span>
            </p>
            <p className="text-green-600 font-bold">${product.price}</p>
            <button
              onClick={() => handleAddToCart(product.id)}
              className="mt-2 bg-blue-500 text-white px-4 py-2 rounded"
            >
              Add to Cart
            </button>
            <img
              src={`http://localhost:8081/images/${product.imageFilename}`}
              alt={product.name}
              className="w-full h-48 object-cover"
            />
          </li>
        ))}
      </ul>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
    </div>
  );
};

export default ProductList;