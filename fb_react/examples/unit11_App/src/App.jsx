import './App.css';
import { useTheme } from './contexts/ThemeContext';
import { useWeatherContext } from './contexts/WeatherContext';
import CitySearch from './components/CitySearch';
import WeatherCard from './components/WeatherCard';
import ForecastCard from './components/ForecastCard';
import SkeletonCard from './components/SkeletonCard';

function App() {
  const { isDark, toggleTheme } = useTheme();
  const { status } = useWeatherContext();

  return (
    <div className="wt-app">
      <header className="wt-header">
        <h1 className="wt-header__title">🌤 天氣預報</h1>
        <button className="wt-theme-btn" onClick={toggleTheme}>
          {isDark ? '☀️ 亮色' : '🌙 暗色'}
        </button>
      </header>

      <main className="wt-main">
        <CitySearch />
        {status === 'loading' && <SkeletonCard />}
        {status !== 'loading' && (
          <>
            <WeatherCard />
            <ForecastCard />
          </>
        )}
      </main>
    </div>
  );
}

export default App;

