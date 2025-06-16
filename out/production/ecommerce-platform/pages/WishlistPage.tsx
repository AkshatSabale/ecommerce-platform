import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { addToCart } from '../services/cartService';
import Toast from '../components/Toast';
import { Link } from 'react-router-dom';
import StarRating from '../components/StarRating';

interface Product {
  id: number;
  name: string;
  quantity: number;
  price: number;
  imageFilename: string;
  description?: string;
  averageRating?: number;
  reviewCount?: number;
}

interface WishlistResponse {
  productIds: number[];
}

interface AddToCartRequest {
  productId: number;
  quantity: number;
}

const WishlistPage: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchWishlist = async () => {
      try {
        setIsLoading(true);
        const wishlistResponse = await api.get<WishlistResponse>('/wishlist');
        console.log('Wishlist response:', wishlistResponse.data);
        const productIds = wishlistResponse.data.productIds.map(id => Number(id));
        console.log('Product IDs from wishlist:', productIds);

        if (productIds.length === 0) {
          setProducts([]);
          return;
        }

        // Fetch details for each product in the wishlist
        const productPromises = productIds.map(id =>
          api.get<Product>(`/api/products/${id}`)
            .then(res => ({ ...res.data, id }))
            .catch(() => null) // Handle case where product might not exist
        );

        const productsData = (await Promise.all(productPromises)).filter(Boolean) as Product[];
        console.log('Final products data:', productsData);
        setProducts(productsData);
      } catch (error) {
        console.error('Error loading wishlist:', error);
        setToastMessage('Could not load wishlist');
      } finally {
        setIsLoading(false);
      }
    };

    fetchWishlist();
  }, []);

const removeFromWishlist = async (productId: number) => {
  if (!productId) {
    console.error('Product ID is undefined');
    setToastMessage('Invalid product ID');
    return;
  }

  try {
    await api.delete(`/wishlist/items/${productId}`);
    setProducts(products.filter(p => p.id !== productId));
    setToastMessage('Removed from wishlist');
  } catch (error) {
    console.error('Error removing from wishlist:', error);
    setToastMessage('Failed to remove from wishlist');
  }
};

  const clearWishlist = async () => {
    try {
      await api.delete('/wishlist');
      setProducts([]);
      setToastMessage('Wishlist cleared');
    } catch (error) {
      console.error('Error clearing wishlist:', error);
      setToastMessage('Failed to clear wishlist');
    }
  };

  const handleAddToCart = async (productId: number) => {
    console.log('Attempting to add to cart:', productId); // Debug log
    try {
      const response = await addToCart(productId, 1);
      console.log('Add to cart response:', response); // Debug log
      setToastMessage('Item added to cart!');
    } catch (error: any) {
      console.error('Add to cart error:', error); // Debug log
      // ... existing error handling
    }
  };


  if (isLoading) {
    return <div className="p-4">Loading wishlist...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">My Wishlist</h2>
        {products.length > 0 && (
          <button
            onClick={clearWishlist}
            className="px-4 py-2 bg-red-100 text-red-600 rounded-md hover:bg-red-200 transition-colors"
          >
            Clear Wishlist
          </button>
        )}
      </div>

      {products.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">Your wishlist is empty</p>
          <Link
            to="/"
            className="mt-4 inline-block px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Browse Products
          </Link>
        </div>
      ) : (
        <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map((product) => (
            <li key={product.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
              <div className="relative pb-2/3 h-48">
                <img
                  src={`http://localhost:8081/images/${product.imageFilename}`}
                  alt={product.name}
                  className="w-full h-48 object-cover"
                />
              </div>
              <div className="p-4">
                <Link to={`/products/${product.id}`}>
                  <h3 className="text-lg font-semibold text-gray-800 hover:text-blue-600 mb-2">{product.name}</h3>
                </Link>
                <div className="flex items-center mb-2">
                  <StarRating rating={product.averageRating || 0} />
                  <span className="text-sm text-gray-600 ml-2">
                    {product.averageRating?.toFixed(1)} ({product.reviewCount || 0} reviews)
                  </span>
                </div>
                <div className="flex justify-between items-center mt-3">
                  <p className="text-green-600 font-bold text-lg">${product.price.toFixed(2)}</p>
                  <p className={`text-sm ${product.quantity > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {product.quantity > 0 ? 'In Stock' : 'Out of Stock'}
                  </p>
                </div>
              </div>
              <div className="px-4 pb-4 flex flex-col space-y-2">
                <button
                  onClick={() => handleAddToCart(product.id)}
                  disabled={product.quantity <= 0}
                  className={`w-full py-2 px-4 rounded-md font-medium ${
                    product.quantity > 0
                      ? 'bg-blue-600 hover:bg-blue-700 text-white'
                      : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  }`}
                >
                  Add to Cart
                </button>
                <button
                  onClick={() => removeFromWishlist(product.id)}
                  className="w-full py-2 px-4 rounded-md font-medium bg-gray-100 hover:bg-gray-200 text-gray-700 transition-colors"
                >
                  Remove from Wishlist
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
    </div>
  );
};

export default WishlistPage;