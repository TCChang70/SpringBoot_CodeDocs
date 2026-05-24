// components/WeatherCard.jsx
import { useWeatherContext } from '../contexts/WeatherContext';

function wmoInfo(code) {
  if (code === 0)  return { label: '晴天',   emoji: '☀️' };
  if (code <= 3)   return { label: '多雲',   emoji: '⛅' };
  if (code <= 48)  return { label: '霧',     emoji: '🌫️' };
  if (code <= 55)  return { label: '毛毛雨', emoji: '🌦️' };
  if (code <= 65)  return { label: '雨',     emoji: '🌧️' };
  if (code <= 75)  return { label: '雪',     emoji: '❄️' };
  if (code <= 82)  return { label: '陣雨',   emoji: '🌦️' };
  if (code <= 99)  return { label: '雷暴',   emoji: '⛈️' };
  return { label: '未知', emoji: '🌡️' };
}

export default function WeatherCard() {
  const { weather, city, status, error } = useWeatherContext();

  if (status === 'error') return <p className="wt-error">{error}</p>;
  if (!weather) return null;

  const c = weather.current;
  const { label, emoji } = wmoInfo(c.weather_code);

  return (
    <div className="wt-card">
      <h2 className="wt-card__city">
        {city?.name}
        <span className="wt-card__country">{city?.country}</span>
      </h2>
      <div className="wt-card__emoji">{emoji}</div>
      <div className="wt-card__temp">{c.temperature_2m}°C</div>
      <div className="wt-card__label">{label}</div>
      <div className="wt-card__meta">
        <span>💧 濕度 {c.relative_humidity_2m}%</span>
        <span>💨 風速 {c.wind_speed_10m} km/h</span>
      </div>
    </div>
  );
}
