import { createContext, useContext, useState, useCallback, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // user 物件格式：{ username: "admin", roles: ["ROLE_ADMIN"] }
  const [user, setUser] = useState(null);
  // 初始化時是否還在確認登入狀態（避免未確認前就顯示登入頁）
  const [initializing, setInitializing] = useState(true);

  // ★ App 啟動時嘗試從既有 Session 恢復登入狀態
  // 對應：頁面重整後 JSESSIONID Cookie 還在，但 React 狀態已清空
  useEffect(() => {
    fetch('/api/me', { credentials: 'include' })
      .then(res => res.ok ? res.json() : null)
      .then(data => { if (data) setUser(data); })
      .catch(() => {})           // 未登入時回 401，正常忽略
      .finally(() => setInitializing(false));
  }, []);

  // 登入：儲存後端回傳的用戶資訊
  const login = useCallback((userData) => {
    setUser(userData);
  }, []);

  // 登出：呼叫後端 GET /logout，再清除前端狀態
  // 對應 SecurityConfig: .logoutRequestMatcher("/logout", "GET")
  const logout = useCallback(async () => {
    try {
      await fetch('/logout', { method: 'GET', credentials: 'include' });
    } finally {
      setUser(null);
    }
  }, []);

  // 角色判斷 — 對應 hasRole / hasAnyRole
  // 支援單一字串 "ADMIN" 或陣列 ["USER","ADMIN"]
  const hasRole = useCallback((role) => {
    if (!user?.roles) return false;
    const roleList = Array.isArray(role) ? role : [role];
    return roleList.some(r => user.roles.includes(`ROLE_${r}`));
  }, [user]);

  const value = {
    user,
    login,
    logout,
    hasRole,
    isAuthenticated: !!user,
    initializing,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth 必須在 <AuthProvider> 內使用');
  }
  return context;
}