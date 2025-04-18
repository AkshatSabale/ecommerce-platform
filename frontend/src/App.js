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
      <h1 className="w-full text-center text-3xl font-bold text-gray-800 mb-6">All Products</h1>
      <div className="min-h-screen bg-gray-100 flex flex-wrap justify-center gap-6 p-6">
        {products.map((item) => (
          <ProductCard key={item.id} product={item} />
        ))}
      </div>
    </div>
  );
}

export default App;