import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const SignUpPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/auth/signup', { username, email, password });
      alert('Check your email for verification code');
      navigate('/verify');
    } catch (err) {
      alert('Signup failed');
    }
  };

  return (
    <form onSubmit={handleSignUp} className="p-4">
      <h2 className="text-xl font-bold mb-4">Sign Up</h2>
      <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Username" required className="block mb-2 p-2 border" />
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required className="block mb-2 p-2 border" />
      <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="Password" required className="block mb-2 p-2 border" />
      <button type="submit" className="bg-green-500 text-white px-4 py-2">Sign Up</button>
    </form>
  );
};

export default SignUpPage;