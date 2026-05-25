// hooks/useLocalStorage.js
import { useState } from 'react';

function useLocalStorage(key, initialValue) {
  // 初始化時從 localStorage 讀取
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      console.log("Get :"+item);
      return item ? JSON.parse(item) : initialValue;
    } catch {
      return initialValue;
    }
  });

  // 同步更新 state 和 localStorage
  const setValue = (value) => {
    try {
      setStoredValue(value);
      console.log("Save :"+value);
      window.localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('localStorage 寫入失敗：', error);
    }
  };

  return [storedValue, setValue];
}

export default useLocalStorage;