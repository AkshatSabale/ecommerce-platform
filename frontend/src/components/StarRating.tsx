import React from 'react';

interface StarRatingProps {
  rating: number;
}

const StarRating: React.FC<StarRatingProps> = ({ rating }) => {
  const fullStars = Math.floor(rating);
  const halfStar = rating % 1 >= 0.5;
  const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

  return (
    <div className="flex items-center space-x-1">
      {Array(fullStars)
        .fill(null)
        .map((_, i) => (
          <span key={`full-${i}`} className="text-yellow-400">★</span>
        ))}
      {halfStar && <span className="text-yellow-400">☆</span>}
      {Array(emptyStars)
        .fill(null)
        .map((_, i) => (
          <span key={`empty-${i}`} className="text-gray-300">★</span>
        ))}
    </div>
  );
};

export default StarRating;