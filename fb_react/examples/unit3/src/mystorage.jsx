// 使用 useLocalStorage — 用法和 useState 幾乎一樣！
import useLocalStorage from './useLocalStorage';

function MySettings() {
  const [theme, setTheme] = useLocalStorage('theme', 'light');
  const [language, setLanguage] = useLocalStorage('language', 'zh-TW');

  const bgColor = theme === 'dark' ? '#222' : '#fff';
  const textColor = theme === 'dark' ? '#fff' : '#222';

  return (
    <div style={{ backgroundColor: bgColor, color: textColor, padding: '20px', minHeight: '100vh' }}>
      <p>目前主題：{theme}</p>
      <button onClick={() => setTheme('dark')}>深色模式</button>
      <button onClick={() => setTheme('light')}>淺色模式</button>

      <p>語言：{language}</p>
      <button onClick={() => setLanguage('en-US')}>English</button>
      <button onClick={() => setLanguage('zh-TW')}>繁體中文</button>
    </div>
  );
}

export default MySettings;