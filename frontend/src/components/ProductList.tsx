import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import api from '../services/api';
import { addToCart } from '../services/cartService';
import Toast from './Toast';
import { Link } from 'react-router-dom';
import StarRating from './StarRating';

interface Product {
  id: number;
  name: string;
  quantity: number;
  price: number;
  imageFilename: string;
  averageRating?: number;
  reviewCount?: number;
}

const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const location = useLocation();

  const fetchProducts = async (query: string = '') => {
    try {
      const response = query
        ? await api.get<Product[]>(`/api/products/search?query=${encodeURIComponent(query)}`)
        : await api.get<Product[]>('/api/products');

      const productsWithRatings = await Promise.all(
        response.data.map(async (product) => {
          try {
            const [avgRes, reviewsRes] = await Promise.all([
              api.get<number>(`/reviews/product/${product.id}/average`),
              api.get(`/reviews/product/${product.id}`),
            ]);

            return {
              ...product,
              averageRating: avgRes.data ?? 0,
              reviewCount: reviewsRes.data.length,
            };
          } catch (e) {
            return { ...product, averageRating: 0, reviewCount: 0 };
          }
        })
      );

      setProducts(productsWithRatings);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

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

  const handleAddToWishlist = async (productId: number) => {
    try {
      await api.post('/wishlist/items', { productId });
      setToastMessage('Item added to wishlist!');
    } catch (error: any) {
      if (error.response && error.response.status === 403) {
        setToastMessage('You need to log in to add items to your wishlist.');
      } else {
        setToastMessage('Something went wrong. Please try again.');
      }
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">Our Products</h1>

      <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {products.map((product) => (
          <li key={product.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <Link to={`/products/${product.id}`} className="block">
              <div className="relative pb-2/3 h-48">
                <img
                  src={`http://localhost:8081/images/${product.imageFilename}`}
                  alt={product.name}
                  className="w-full h-48 object-cover"
                />
              </div>
              <div className="p-4">
                <h2 className="text-lg font-semibold text-gray-800 hover:text-blue-600 mb-2">{product.name}</h2>
                <div className="flex items-center mb-2">
                  <StarRating rating={product.averageRating || 0} />
                  <span className="text-sm text-gray-600 ml-2">
                    {product.averageRating?.toFixed(1)} ({product.reviewCount} reviews)
                  </span>
                </div>
                <div className="flex justify-between items-center mt-3">
                  <p className="text-green-600 font-bold text-lg">${product.price.toFixed(2)}</p>
                  <p className={`text-sm ${product.quantity > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {product.quantity > 0 ? 'In Stock' : 'Out of Stock'}
                  </p>
                </div>
              </div>
            </Link>
            <div className="px-4 pb-4 flex space-x-2">
              <button
                onClick={() => handleAddToCart(product.id)}
                disabled={product.quantity <= 0}
                className={`flex-1 py-2 px-4 rounded-md font-medium ${
                  product.quantity > 0
                    ? 'bg-blue-600 hover:bg-blue-700 text-white'
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
              >
                Add to Cart
              </button>
              <button
                onClick={() => handleAddToWishlist(product.id)}
                className="flex-1 py-2 px-4 rounded-md font-medium bg-pink-100 hover:bg-pink-200 text-pink-700 border border-pink-200"
              >
                ♡ Wishlist
              </button>
            </div>
          </li>
        ))}
      </ul>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
    </div>
  );
};

export default ProductList;