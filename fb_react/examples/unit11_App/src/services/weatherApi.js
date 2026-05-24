// services/weatherApi.js
import axios from 'axios';

const geocodingApi = axios.create({
  baseURL: 'https://geocoding-api.open-meteo.com/v1',
});

const weatherApi = axios.create({
  baseURL: 'https://api.open-meteo.com/v1',
});

// 城市名稱 → 座標
export async function searchCity(name) {
  const { data } = await geocodingApi.get('/search', {
    params: { name, count: 5, language: 'zh', format: 'json' },
  });
  return data.results ?? [];
}

// 座標 → 天氣
export async function getWeather(latitude, longitude) {
  const { data } = await weatherApi.get('/forecast', {
    params: {
      latitude,
      longitude,
      current: 'temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m',
      daily: 'weather_code,temperature_2m_max,temperature_2m_min',
      timezone: 'Asia/Taipei',
      forecast_days: 6,
    },
  });
  return data;
}