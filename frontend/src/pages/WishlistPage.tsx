import React, { useEffect, useState } from 'react';
import api from '../services/api';

const WishlistPage: React.FC = () => {
  const [productIds, setProductIds] = useState<number[]>([]);

  useEffect(() => {
    api.get('/wishlist')
       .then(res => setProductIds(res.data.productIds))
       .catch(() => alert('Could not load wishlist'));
  }, []);

  const remove = (pid: number) => {
    api.delete(`/wishlist/items/${pid}`)
       .then(res => setProductIds(res.data.productIds));
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">My Wishlist</h2>
      {productIds.length === 0 && <p>No items yet.</p>}
      <ul>
        {productIds.map(pid => (
          <li key={pid} className="flex justify-between mb-2">
            <span>Product #{pid}</span>
            <button onClick={() => remove(pid)} className="text-red-600">Remove</button>
          </li>
        ))}
      </ul>
    </div>
  );
};
export default WishlistPage;