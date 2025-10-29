import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts:true,
    host: '0.0.0.0',
    port: 5000,
    proxy: {
      '/api': {
        target: 'https://mywatch-4117.onrender.com/',
        changeOrigin: true
      },
      '/uploads': {
        target: 'https://mywatch-4117.onrender.com/',
        changeOrigin: true
      }
    }
  }
})
