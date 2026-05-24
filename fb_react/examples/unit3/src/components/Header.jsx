// components/Header.jsx — 深層子元件直接取用
import { useTheme } from '../contexts/ThemeContext';

function Header() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header style={{ background: theme === 'dark' ? '#333' : '#fff' }}>
      <h1>我的 App</h1>
      <button onClick={toggleTheme}>
        切換為 {theme === 'light' ? '深色' : '淺色'} 模式
      </button>
    </header>
  );
}

export default Header;