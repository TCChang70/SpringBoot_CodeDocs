// hooks/useWeather.js
import { useReducer } from 'react';
import { searchCity, getWeather } from '../services/weatherApi';

// 定義所有可能的狀態
const initialState = {
  status: 'idle',     // 'idle' | 'loading' | 'success' | 'error'
  weather: null,
  city: null,
  error: null,
};

function reducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, status: 'loading', error: null };
    case 'FETCH_SUCCESS':
      return { status: 'success', weather: action.weather, city: action.city, error: null };
    case 'FETCH_ERROR':
      return { ...state, status: 'error', error: action.error };
    default:
      return state;
  }
}

export function useWeather() {
  const [state, dispatch] = useReducer(reducer, initialState);

  async function fetchWeather(cityName) {
    dispatch({ type: 'FETCH_START' });

    try {
      const results = await searchCity(cityName);
      if (results.length === 0) throw new Error(`找不到城市：${cityName}`);

      const { latitude, longitude, name, country } = results[0];
      const weather = await getWeather(latitude, longitude);

      dispatch({ type: 'FETCH_SUCCESS', weather, city: { name, country } });
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', error: err.message });
    }
  }

  async function fetchWeatherByCoords(latitude, longitude, name = '', country = '') {
    dispatch({ type: 'FETCH_START' });
    try {
      const weather = await getWeather(latitude, longitude);
      dispatch({ type: 'FETCH_SUCCESS', weather, city: { name, country } });
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', error: err.message });
    }
  }

  return { ...state, fetchWeather, fetchWeatherByCoords };
}