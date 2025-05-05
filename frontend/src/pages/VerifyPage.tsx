import React, { useState } from 'react';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

const VerifyPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const navigate = useNavigate();

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/auth/verify', { email, verificationCode: code });
      alert('Account verified! You can now log in.');
      navigate('/login');
    } catch (err) {
      alert('Verification failed');
    }
  };

  return (
    <form onSubmit={handleVerify} className="p-4">
      <h2 className="text-xl font-bold mb-4">Verify Account</h2>
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required className="block mb-2 p-2 border" />
      <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="Verification Code" required className="block mb-2 p-2 border" />
      <button type="submit" className="bg-purple-500 text-white px-4 py-2">Verify</button>
    </form>
  );
};

export default VerifyPage;