import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import StarRating from '../components/StarRating';
import Toast from '../components/Toast';

interface ProductDetail {
  id: number;
  name: string;
  quantity: number;
  price: number;
  description: string;
  imageFilename: string;
  averageRating?: number;
}

interface ProductReview {
  username: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
}

const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [reviews, setReviews] = useState<ProductReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  useEffect(() => {
    const fetchProductDetails = async () => {
      try {
        setLoading(true);

        // Fetch product details
        const productResponse = await api.get<ProductDetail>(`/api/products/${id}`);

        // Fetch reviews in parallel
        const reviewsResponse = await api.get<ProductReview[]>(`/reviews/product/${id}`);

        // Fetch average rating
        const avgRatingResponse = await api.get<number>(`/reviews/product/${id}/average`);

        setProduct({
          ...productResponse.data,
          averageRating: avgRatingResponse.data || 0
        });
        setReviews(reviewsResponse.data);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch product:', err);
        setError('Product not found or failed to load');
      } finally {
        setLoading(false);
      }
    };

    fetchProductDetails();
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;

    try {
      await api.post('/api/cart/add', { productId: product.id, quantity: 1 });
      setToastMessage('Item added to cart!');
    } catch (error: any) {
      if (error.response?.status === 403) {
        setToastMessage('You need to log in to add items to your cart.');
      } else {
        setToastMessage('Failed to add item to cart. Please try again.');
      }
    }
  };

  if (loading) {
    return <div className="p-4">Loading product details...</div>;
  }

  if (error || !product) {
    return (
      <div className="p-4 text-center">
        <h2 className="text-xl font-bold text-red-500">{error || 'Product not found'}</h2>
        <p className="mt-2">The product you're looking for doesn't exist or may have been removed.</p>
        <a href="/products" className="text-blue-500 underline mt-4 inline-block">
          Back to products
        </a>
      </div>
    );
  }

  return (
    <div className="p-4 max-w-6xl mx-auto">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Product Image */}
        <div className="bg-white p-4 rounded-lg shadow">
          <img
            src={`http://localhost:8081/images/${product.imageFilename}`}
            alt={product.name}
            className="w-full h-auto max-h-96 object-contain"
          />
        </div>

        {/* Product Info */}
        <div className="space-y-4">
          <h1 className="text-3xl font-bold">{product.name}</h1>

          <div className="flex items-center space-x-2">
            <StarRating rating={product.averageRating ?? 0} />
            <span className="text-gray-600">
              {product.averageRating!.toFixed(1)} ({reviews.length} reviews)
            </span>
          </div>

          <p className="text-2xl font-bold text-green-600">${product.price}</p>

          <p className="text-gray-700">
            {product.description || 'No description available.'}
          </p>

          <p className={product.quantity > 0 ? 'text-green-600' : 'text-red-600'}>
            {product.quantity > 0 ? 'In Stock' : 'Out of Stock'}
          </p>

          <button
            onClick={handleAddToCart}
            disabled={product.quantity <= 0}
            className={`px-6 py-2 rounded-md font-medium ${
              product.quantity > 0
                ? 'bg-blue-500 text-white hover:bg-blue-600'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {product.quantity > 0 ? 'Add to Cart' : 'Out of Stock'}
          </button>
        </div>
      </div>

      {/* Reviews Section */}
      <div className="mt-12">
        <h2 className="text-2xl font-bold mb-6">Customer Reviews</h2>

        {reviews.length === 0 ? (
          <p className="text-gray-500">No reviews yet. Be the first to review!</p>
        ) : (
          <div className="space-y-6">
            {reviews.map((review, index) => (
              <div key={index} className="border-b pb-4">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-medium">{review.username}</h3>
                    <div className="flex items-center space-x-1 mt-1">
                      <StarRating rating={review.rating} />
                      <span className="text-sm text-gray-500">
                        {new Date(review.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                  {review.updatedAt !== review.createdAt && (
                    <span className="text-xs text-gray-400">
                      (edited)
                    </span>
                  )}
                </div>
                <p className="mt-2 text-gray-700">{review.comment}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {toastMessage && <Toast message={toastMessage} onClose={() => setToastMessage(null)} />}
    </div>
  );
};

export default ProductDetailPage;