# SMBMS Frontend (Vue 3 + TS)

## 开发

```bash
cd frontend
npm install
npm run dev
# 浏览器打开 http://localhost:5173
```

后端需先启动（`http://localhost:8080/smbms`），dev 模式下 `/api/**` 会被自动代理过去。

## 构建

```bash
npm run build
# 产物在 dist/
```

## 与后端集成

- 鉴权：登录后 JWT token 存 Pinia + localStorage；axios 拦截器自动加 `Authorization: Bearer <token>`；401 自动跳登录页。
- 接口路径与 `com.viper.controller` 下的 REST 控制器一一对应。
