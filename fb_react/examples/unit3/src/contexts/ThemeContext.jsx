// contexts/ThemeContext.jsx
import { createContext, useContext, useState } from 'react';

// 1. 建立 Context（給定預設值）
const ThemeContext = createContext({
  theme: 'light',
  toggleTheme: () => {},
});

// 2. 建立 Provider 元件（包裝要共享資料的範圍）
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// 3. 建立自訂 Hook 方便使用（可選但推薦）
// eslint-disable-next-line react-refresh/only-export-components
export function useTheme() {
  return useContext(ThemeContext);
}