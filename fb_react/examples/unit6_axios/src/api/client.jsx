// src/api/client.js（加入攔截器）
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://fakestoreapi.com',
  timeout: 10000,
});

// 請求攔截器（Request Interceptor）：送出前加入 Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('[Interceptor] ⚠️ 無 token，Authorization header 不會加入');
    }
    // 觀察用：axios v1.x 需用 toJSON() 或直接讀取欄位
    console.log('[Request]', config.method?.toUpperCase(), config.url);
    console.log('[Authorization]', config.headers.get('Authorization') ?? '（未設定）');
    return config; // ⚠️ 必須 return config，否則請求不會送出
  },
  (error) => Promise.reject(error)
);

// 回應攔截器（Response Interceptor）：統一處理錯誤
apiClient.interceptors.response.use(
  (response) => response, // 成功（2xx）直接回傳
  (error) => {
    if (error.response?.status === 401) {
      // Token 過期 → 導回登入頁
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;