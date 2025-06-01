import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/api/auth/login', { email, password });
      login(response.data.token);
      navigate('/'); // Redirect to homepage or dashboard
    } catch (err) {
      alert('Login failed');
    }
  };

  return (
    <form onSubmit={handleLogin} className="p-4">
      <h2 className="text-xl font-bold mb-4">Login</h2>
      <input
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
        className="block mb-2 p-2 border"
      />
      <input
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        type="password"
        placeholder="Password"
        required
        className="block mb-2 p-2 border"
      />
      <button type="submit" className="bg-blue-500 text-white px-4 py-2">
        Login
      </button>
      <p className="mt-2">
        Don't have an account?{' '}
        <a href="/signup" className="text-blue-600 underline">Sign up</a>
      </p>
    </form>
  );
};

export default LoginPage;