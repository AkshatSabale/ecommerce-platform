import React, { useState } from 'react';

interface ReviewFormProps {
  onSubmit: (rating: number, comment: string) => void;
  onCancel: () => void;
  initialRating: number;
  initialComment: string;
}

const ReviewForm: React.FC<ReviewFormProps> = ({
  onSubmit,
  onCancel,
  initialRating,
  initialComment
}) => {
  const [rating, setRating] = useState(initialRating);
  const [comment, setComment] = useState(initialComment);

  return (
    <div className="mb-6 p-4 bg-gray-50 rounded-lg">
      <h3 className="font-medium mb-2">Write Your Review</h3>
      <div className="mb-4">
        <label className="block mb-1">Rating</label>
        <div className="flex">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              onClick={() => setRating(star)}
              className={`text-2xl ${star <= rating ? 'text-yellow-400' : 'text-gray-300'}`}
            >
              ★
            </button>
          ))}
        </div>
      </div>
      <div className="mb-4">
        <label className="block mb-1">Comment</label>
        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          className="w-full p-2 border rounded"
          rows={4}
        />
      </div>
      <div className="flex space-x-2">
        <button
          onClick={() => onSubmit(rating, comment)}
          disabled={rating === 0 || !comment.trim()}
          className={`px-4 py-2 rounded ${
            rating === 0 || !comment.trim()
              ? 'bg-gray-300 cursor-not-allowed'
              : 'bg-blue-500 text-white hover:bg-blue-600'
          }`}
        >
          Submit
        </button>
        <button
          onClick={onCancel}
          className="px-4 py-2 border rounded hover:bg-gray-100"
        >
          Cancel
        </button>
      </div>
    </div>
  );
};

export default ReviewForm;