// AppContext.jsx — 用 Provider 包住整個應用
import { ThemeProvider } from './contexts/ThemeContext';
import Header from './components/Header';
import Content from './components/Content';

function AppContext() {
  return (
    <ThemeProvider>
      <Header />
      <Content />
    </ThemeProvider>
  );
}

export default AppContext;