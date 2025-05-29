import axios from 'axios';

const api = axios.create({
  baseURL: "http://localhost:8081",
  withCredentials: true,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');

    const isExpired = () => {
      try {
        if (!token) return true;
        const { exp } = JSON.parse(atob(token.split('.')[1]));
        return Date.now() >= exp * 1000;
      } catch {
        return true; // Invalid token format
      }
    };

    if (token && !isExpired()) {
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      localStorage.removeItem("jwtToken");
      // Optionally redirect to login (client-specific)
      // window.location.href = "/login"; // Uncomment if needed
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default api;