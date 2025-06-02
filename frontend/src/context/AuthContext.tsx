import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  userId: string | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  token: null,
  isAuthenticated: false,
  isAdmin: false,
  loading: true,
  userId: null,
  login: () => {},
  logout: () => {}
});

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [userId, setUserId] = useState<string | null>(null);

  // Initialize from localStorage
  useEffect(() => {
    const storedToken = localStorage.getItem('jwtToken');
    if (storedToken) {
      try {
        const decoded = jwtDecode<{ sub: string; roles?: string[] }>(storedToken);
        setToken(storedToken);
        setUserId(decoded.sub);
      } catch (error) {
        localStorage.removeItem('jwtToken');
      }
    }
    setLoading(false);
  }, []);

  const isAuthenticated = !!token;
  const isAdmin = token ?
    (jwtDecode<{ roles?: string[] }>(token).roles?.includes('ROLE_ADMIN') || false) :
    false;

  const login = (newToken: string) => {
    localStorage.setItem('jwtToken', newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('jwtToken');
    setToken(null);
  };

  const contextValue = {
    token,
    isAuthenticated,
    isAdmin,
    loading,
    userId,
    login,
    logout
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};