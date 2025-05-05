import axios from 'axios';

const api = axios.create({
  baseURL: "http://localhost:8081",
  withCredentials: true, // Include cookies if needed
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;