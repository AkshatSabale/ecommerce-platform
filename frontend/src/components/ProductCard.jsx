import React from 'react';

const ProductCard = ({ product }) => {
  return (
    <div style={{ border: '1px solid #ccc', padding: '16px', width: '200px', margin: '10px' }}>
      <img src={product.image} alt={product.name} style={{ width: '100%' }} />
      <h2>{product.name}</h2>
      <p>₹{product.price}</p>
      <button>Buy Now</button>
    </div>
  );
};

export default ProductCard;