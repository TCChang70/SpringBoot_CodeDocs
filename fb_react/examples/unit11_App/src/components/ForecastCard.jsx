// components/ForecastCard.jsx
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

export default function ForecastCard() {
  const { weather } = useWeatherContext();
  if (!weather) return null;

  const { daily } = weather;

  return (
    <div className="wt-forecast">
      {daily.time.map((date, i) => {
        const { emoji, label } = wmoInfo(daily.weather_code[i]);
        const day = new Date(date).toLocaleDateString('zh-TW', {
          weekday: 'short',
          month: 'numeric',
          day: 'numeric',
        });
        return (
          <div key={date} className="wt-forecast__item">
            <div className="wt-forecast__day">{day}</div>
            <div className="wt-forecast__emoji">{emoji}</div>
            <div className="wt-forecast__label">{label}</div>
            <div className="wt-forecast__max">{daily.temperature_2m_max[i]}°</div>
            <div className="wt-forecast__min">{daily.temperature_2m_min[i]}°</div>
          </div>
        );
      })}
    </div>
  );
}
