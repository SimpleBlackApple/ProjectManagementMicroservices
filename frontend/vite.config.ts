import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [tsconfigPaths({ root: __dirname }), react()],
  server: {
    proxy: {
      // Auth相关路由
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 特殊处理 projects/:id/tasks 路由
      '^/api/projects/\\d+/tasks': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      // 常规路由
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/projects': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/tasks': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          antd: ["antd"],
        },
      },
    },
  },
});
