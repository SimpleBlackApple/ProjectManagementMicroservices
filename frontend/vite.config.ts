import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [tsconfigPaths({ root: __dirname }), react(),
    // {
    //   name: 'proxy-logger',
    //   configureServer(server) {
    //     server.middlewares.use((req, res, next) => {
    //       const startTime = Date.now();
    //
    //       res.on('finish', () => {
    //         const duration = Date.now() - startTime;
    //         console.log('\x1b[36m%s\x1b[0m', '📝 请求日志:', {
    //           路径: req.url,
    //           方法: req.method,
    //           状态: res.statusCode,
    //           耗时: `${duration}ms`,
    //           时间: new Date().toLocaleTimeString()
    //         });
    //       });
    //
    //       next();
    //     });
    //   }
    // }
    ,],
  server: {
    proxy: {
      // Auth相关路由
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // 特殊处理 projects/:id/tasks、projects/:id/sprints 和 projects/:id/members 路由
      '^/api/projects/\\d+/tasks': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '^/api/projects/\\d+/sprints': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '^/api/projects/\\d+/members': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/tasks': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        // configure: (proxy, _options) => {
        //   proxy.on("error", (err, _req, _res) => {
        //     console.log("proxy error", err);
        //   });
        //   proxy.on("proxyReq", (proxyReq, req, _res) => {
        //     console.log(
        //       "Sending Request:",
        //       req.method,
        //       req.url,
        //       " => TO THE TARGET =>  ",
        //       proxyReq.method,
        //       proxyReq.protocol,
        //       proxyReq.host,
        //       proxyReq.path,
        //       JSON.stringify(proxyReq.getHeaders()),
        //     );
        //   });
        //   proxy.on("proxyRes", (proxyRes, req, _res) => {
        //     console.log(
        //       "Received Response from the Target:",
        //       proxyRes.statusCode,
        //       req.url,
        //       JSON.stringify(proxyRes.headers),
        //     );
        //   });
        // },
      },
      '/api/sprints': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/projects': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // 常规路由
      '/api/users': {
        target: 'http://localhost:8081',
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
