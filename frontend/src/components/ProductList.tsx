import React, { useEffect, useState } from 'react';
import api from '../services/api';

interface Product {
  id: number;
  name: string;
  quantity: number;
  price: number;
}

const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await api.get<Product[]>('/products');
        setProducts(response.data);
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };

    fetchProducts();
  }, []);

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold mb-4">Products</h1>
      <ul className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {products.map((product) => (
          <li key={product.id} className="border p-4 rounded">
            <h2 className="text-xl font-semibold">{product.name}</h2>
            <p className="mb-1">Quantity: <span className="font-medium">{product.quantity}</span></p>
            <p className="text-green-600 font-bold">${product.price}</p>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ProductList;