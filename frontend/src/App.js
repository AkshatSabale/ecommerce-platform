import React from 'react';
import ProductCard from './components/ProductCard';

function App() {
  const products = [
    {
      id: 1,
      name: "Wireless Mouse",
      price: 799,
      image: "https://via.placeholder.com/150"
    },
    {
      id: 2,
      name: "Keyboard",
      price: 1199,
      image: "https://via.placeholder.com/150"
    },
    {
      id: 3,
      name: "USB-C Hub",
      price: 999,
      image: "https://via.placeholder.com/150"
    }
  ];

  return (
    <div>
      <h1 style={{ padding: '10px' }}>All Products</h1>
      <div style={{ display: 'flex' }}>
        {products.map((item) => (
          <ProductCard key={item.id} product={item} />
        ))}
      </div>
    </div>
  );
}

export default App;