// components/Content.jsx — 深層子元件直接消費 Context，無需 props 傳遞
import { useTheme } from '../contexts/ThemeContext';

function Content() {
  const { theme } = useTheme();

  const isDark = theme === 'dark';

  const styles = {
    wrapper: {
      padding: '24px',
      minHeight: '200px',
      background: isDark ? '#1e1e1e' : '#f9f9f9',
      color:      isDark ? '#e0e0e0' : '#222',
      borderTop:  `2px solid ${isDark ? '#555' : '#ddd'}`,
      transition: 'background 0.3s, color 0.3s',
    },
    badge: {
      display: 'inline-block',
      padding: '4px 12px',
      borderRadius: '999px',
      fontSize: '13px',
      fontWeight: 600,
      background: isDark ? '#444' : '#e0e0e0',
      color:      isDark ? '#fff' : '#333',
      marginBottom: '12px',
    },
    card: {
      padding: '16px',
      borderRadius: '8px',
      border: `1px solid ${isDark ? '#444' : '#ddd'}`,
      background: isDark ? '#2a2a2a' : '#fff',
      marginTop: '16px',
    },
  };

  return (
    <main style={styles.wrapper}>
      <span style={styles.badge}>目前主題：{isDark ? '🌙 深色' : '☀️ 淺色'}</span>

      <p>
        這個 <code>&lt;Content&gt;</code> 元件透過 <strong>useTheme()</strong> 直接
        從 ThemeContext 取得 <code>theme</code> 值，不需要父層透過 props 傳入。
      </p>

      <div style={styles.card}>
        <h3 style={{ margin: '0 0 8px' }}>Context 運作說明</h3>
        <ul style={{ margin: 0, paddingLeft: '20px', lineHeight: '1.8' }}>
          <li><code>createContext()</code> — 建立 Context 容器</li>
          <li><code>&lt;ThemeProvider&gt;</code> — 在 AppContext 最外層提供資料</li>
          <li><code>useTheme()</code> — 任意子元件消費，不需要 props 傳遞</li>
        </ul>
      </div>
    </main>
  );
}

export default Content;
