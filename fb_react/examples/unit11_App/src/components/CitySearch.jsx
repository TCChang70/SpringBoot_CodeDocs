// components/CitySearch.jsx
import { useState } from 'react';
import { useWeatherContext } from '../contexts/WeatherContext';

export default function CitySearch() {
  const [input, setInput] = useState('');
  const { fetchWeather, status } = useWeatherContext();

  function handleSubmit(e) {
    e.preventDefault();
    if (input.trim()) fetchWeather(input.trim());
  }

  return (
    <form className="wt-search" onSubmit={handleSubmit}>
      <input
        className="wt-search__input"
        value={input}
        onChange={e => setInput(e.target.value)}
        placeholder="輸入城市名稱搜尋..."
        disabled={status === 'loading'}
      />
      <button
        className="wt-search__btn"
        type="submit"
        disabled={status === 'loading' || !input.trim()}
      >
        搜尋
      </button>
    </form>
  );
}
