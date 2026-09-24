// 認證狀態管理（React Context）
// - 統一持有 token 與目前已登入的使用者
// - 提供 login() / logout() 供頁面呼叫
import { createContext, useContext, useEffect, useState } from 'react';
import { getToken, setToken } from '../api/client.js';
import { login as apiLogin } from '../api/auth.js';
import { me } from '../api/users.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false); // true 表示已完成「用 Token 回填使用者」的初始化

  useEffect(() => {
    let cancelled = false;
    if (getToken()) {
      me()
        .then((u) => !cancelled && setUser(u))
        .catch(() => {
          setToken(null); // token 無效/使用者不存在 → 清除
        })
        .finally(() => !cancelled && setReady(true));
    } else {
      setReady(true);
    }
    return () => {
      cancelled = true;
    };
  }, []);

  const value = {
    user,
    ready,
    isLoggedIn: !!user,
    login: async (username, password) => {
      const res = await apiLogin({ username, password });
      setToken(res.token);
      setUser(res.user);
      return res;
    },
    logout: () => {
      setToken(null);
      setUser(null);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}