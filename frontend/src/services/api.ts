import axios from 'axios';

const api = axios.create({
  baseURL: "http://localhost:8081",
  withCredentials: true, // Include cookies if needed
});

export default api;