import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import StarRating from '../components/StarRating';
import Toast from '../components/Toast';
import VerifiedPurchaseBadge from '../components/VerifiedPurchaseBadge';
import { useAuth } from '../context/AuthContext';
import ReviewForm from '../components/ReviewForm';

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
  id: number;
  username: string;
  rating: number;
  userId: number | null;
  comment: string;
  createdAt: string;
  updatedAt: string;
  verifiedPurchase: boolean;
}

const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [reviews, setReviews] = useState<ProductReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const { userId, isAuthenticated } = useAuth();
  const [userReview, setUserReview] = useState<ProductReview | null>(null);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [isDeleting, setIsDeleting] = useState<number | null>(null);

  useEffect(() => {
    const fetchProductDetails = async () => {
      try {
        setLoading(true);
        const [productResponse, reviewsResponse, avgRatingResponse] = await Promise.all([
          api.get<ProductDetail>(`/api/products/${id}`),
          api.get<ProductReview[]>(`/reviews/product/${id}`),
          api.get<number>(`/reviews/product/${id}/average`)
        ]);

        setProduct({
          ...productResponse.data,
          averageRating: avgRatingResponse.data || 0
        });
        setReviews(reviewsResponse.data);
        setError(null);

        if (isAuthenticated && userId) {
          const userReview = reviewsResponse.data.find(review =>
            review.userId?.toString() === userId.toString()
          );
          setUserReview(userReview || null);
        }
      } catch (err) {
        console.error('Failed to fetch product:', err);
        setError('Product not found or failed to load');
      } finally {
        setLoading(false);
      }
    };

    fetchProductDetails();
  }, [id, isAuthenticated, userId]);

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

  const handleDeleteReview = async (reviewId: number) => {
    if (!window.confirm('Are you sure you want to delete this review?')) return;

    try {
      setIsDeleting(reviewId);
      await api.delete(`/reviews/${product?.id}`);
      setToastMessage('Review deleted successfully!');

      const [reviewsResponse, avgRatingResponse] = await Promise.all([
        api.get<ProductReview[]>(`/reviews/product/${id}`),
        api.get<number>(`/reviews/product/${id}/average`)
      ]);

      setReviews(reviewsResponse.data);
      setProduct(prev => prev ? {
        ...prev,
        averageRating: avgRatingResponse.data || 0
      } : null);
      setUserReview(prev => prev?.id === reviewId ? null : prev);
    } catch (error) {
      console.error('Failed to delete review:', error);
      setToastMessage('Failed to delete review. Please try again.');
    } finally {
      setIsDeleting(null);
    }
  };

  const handleSubmitReview = async (rating: number, comment: string) => {
    try {
      if (!id) {
        setToastMessage('Product ID not available');
        return;
      }

      await api.post('/reviews', {
        productId: id,
        rating,
        comment
      });

      setToastMessage('Review submitted successfully!');
      setShowReviewForm(false);

      const [reviewsResponse, avgRatingResponse] = await Promise.all([
        api.get<ProductReview[]>(`/reviews/product/${id}`),
        api.get<number>(`/reviews/product/${id}/average`)
      ]);

      setReviews(reviewsResponse.data);
      setProduct(prev => prev ? {
        ...prev,
        averageRating: avgRatingResponse.data || 0
      } : null);

      if (userId) {
        const userReview = reviewsResponse.data.find(review =>
          review.userId?.toString() === userId.toString()
        );
        setUserReview(userReview || null);
      }
    } catch (error) {
      console.error('Failed to submit review:', error);
      setToastMessage('Failed to submit review. Please try again.');
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
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Customer Reviews</h2>
          {isAuthenticated && !userReview && (
            <button
              onClick={() => setShowReviewForm(true)}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Write a Review
            </button>
          )}
        </div>

        {showReviewForm && (
          <ReviewForm
            onSubmit={handleSubmitReview}
            onCancel={() => setShowReviewForm(false)}
            initialRating={userReview?.rating || 0}
            initialComment={userReview?.comment || ''}
          />
        )}

        {userReview && (
          <div className="mb-6 p-4 bg-gray-50 rounded-lg">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-medium">Your Review</h3>
                <div className="flex items-center space-x-1 mt-1">
                  <StarRating rating={userReview.rating} />
                  <span className="text-sm text-gray-500">
                    {new Date(userReview.createdAt).toLocaleDateString()}
                  </span>
                  {userReview.updatedAt !== userReview.createdAt && (
                    <span className="text-xs text-gray-400">(edited)</span>
                  )}
                </div>
                <p className="mt-2 text-gray-700">{userReview.comment}</p>
              </div>
              <div className="flex space-x-2">
                <button
                  onClick={() => setShowReviewForm(true)}
                  className="text-blue-500 hover:text-blue-700 text-sm"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDeleteReview(userReview.id)}
                  disabled={isDeleting === userReview.id}
                  className={`text-red-500 hover:text-red-700 text-sm ${
                    isDeleting === userReview.id ? 'opacity-50 cursor-not-allowed' : ''
                  }`}
                >
                  {isDeleting === userReview.id ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          </div>
        )}

        {reviews.length === 0 ? (
          <p className="text-gray-500">No reviews yet. Be the first to review!</p>
        ) : (
          <div className="space-y-6">
            {reviews
              .filter(review => !userReview || review.id !== userReview.id)
              .map((review) => (
                <div key={review.id} className="border-b pb-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center">
                        <h3 className="font-medium">{review.username}</h3>
                        {review.verifiedPurchase && <VerifiedPurchaseBadge />}
                      </div>
                      <div className="flex items-center space-x-1 mt-1">
                        <StarRating rating={review.rating} />
                        <span className="text-sm text-gray-500">
                          {new Date(review.createdAt).toLocaleDateString()}
                        </span>
                        {review.updatedAt !== review.createdAt && (
                          <span className="text-xs text-gray-400">(edited)</span>
                        )}
                      </div>
                    </div>
                    {userId === review.userId && (
                      <button
                        onClick={() => handleDeleteReview(review.id)}
                        disabled={isDeleting === review.id}
                        className={`text-red-500 hover:text-red-700 text-sm ${
                          isDeleting === review.id ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        {isDeleting === review.id ? 'Deleting...' : 'Delete'}
                      </button>
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