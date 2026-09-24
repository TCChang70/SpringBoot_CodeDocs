import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite 開發伺服器設定：
// - 預設埠 5173（與後端 application.yml 的 CORS allowed-origins 一致）
// - 將 /api、/media、/actuator 請求代理到 Spring Boot(:8080)，開發時不需開 CORS 也能呼叫
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // 綁定 0.0.0.0（IPv4 與雙棧可達）。預設只綁 IPv6(::1) 會導致瀏覽器
    // 以 127.0.0.1 連 localhost 時「連不上/404」，這裡明確改綁全部網卡。
    host: true,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/media': { target: 'http://localhost:8080', changeOrigin: true },
      '/actuator': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});