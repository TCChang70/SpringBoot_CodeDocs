import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // 將 /api 請求代理到後端，避免 CORS 問題
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
