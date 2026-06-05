import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
   server: {
    proxy: {
      // 符合前綴的請求全部轉發到 Spring Boot
      '/login':  { target: 'http://localhost:8080', changeOrigin: true },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/api':    { target: 'http://localhost:8080', changeOrigin: true },
      '/admin':  { target: 'http://localhost:8080', changeOrigin: true },
      '/user':   { target: 'http://localhost:8080', changeOrigin: true },
    }
  }
});
