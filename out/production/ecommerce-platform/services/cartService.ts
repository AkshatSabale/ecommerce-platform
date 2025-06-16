import api from './api';

export const addToCart = async (productId: number, quantity: number = 1) => {
  try {
    const response = await api.post('/cart/items', {
      productId,
      quantity
    });
    return response.data;
  } catch (error) {
    console.error('Error adding to cart:', error);
    throw error;
  }
};