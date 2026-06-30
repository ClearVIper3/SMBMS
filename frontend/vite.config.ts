import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

// 开发期 / api 代理到本地后端，避开 CORS；
// 生产部署时前端构建产物通常与后端同源（或反向代理），不需要 proxy。
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/smbms',
        changeOrigin: true
      }
    }
  }
})
