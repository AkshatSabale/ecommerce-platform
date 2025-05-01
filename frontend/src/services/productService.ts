import api from './api';

export const fetchProducts = async () => {
  const response = await api.get('/products');
  return response.data;
};