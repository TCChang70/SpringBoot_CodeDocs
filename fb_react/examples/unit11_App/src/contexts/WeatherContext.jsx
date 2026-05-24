// contexts/WeatherContext.jsx
import { createContext, useContext, useEffect } from 'react';
import { useWeather } from '../hooks/useWeather';

const WeatherContext = createContext();

// 台北市經緯度
const TAIPEI = { latitude: 25.0478, longitude: 121.5319, name: '台北', country: 'TW' };

export function WeatherProvider({ children }) {
  const weather = useWeather();

  // 初始載入時以台北經緯度取得天氣
  useEffect(() => {
    weather.fetchWeatherByCoords(TAIPEI.latitude, TAIPEI.longitude, TAIPEI.name, TAIPEI.country);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <WeatherContext.Provider value={weather}>
      {children}
    </WeatherContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useWeatherContext = () => useContext(WeatherContext);
