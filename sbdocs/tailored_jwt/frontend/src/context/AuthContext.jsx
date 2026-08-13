import { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('ecom_user') || 'null');
    } catch {
      return null;
    }
  });
  const navigate = useNavigate();

  useEffect(() => {
    const handler = () => {
      setUser(null);
      navigate('/login');
    };
    window.addEventListener('auth:expired', handler);
    return () => window.removeEventListener('auth:expired', handler);
  }, [navigate]);

  const login = async (username, password) => {
    const data = await authApi.login(username, password);
    localStorage.setItem('ecom_token', data.token);
    localStorage.setItem('ecom_user', JSON.stringify({ username: data.username, role: data.role }));
    setUser({ username: data.username, role: data.role });
    return data;
  };

  const register = async (username, password, role) => {
    await authApi.register(username, password, role);
  };

  const logout = () => {
    localStorage.removeItem('ecom_token');
    localStorage.removeItem('ecom_user');
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider
      value={{ user, isAdmin: user?.role === 'ADMIN', login, register, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
