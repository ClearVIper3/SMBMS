# 前端多阶段 Dockerfile：Node 构建 + nginx 托管
FROM node:20-alpine AS builder
WORKDIR /workspace

COPY package.json ./
RUN npm install --no-audit --no-fund

COPY . ./
RUN npm run build

# ---------- 运行 ----------
FROM nginx:1.27-alpine
COPY --from=builder /workspace/dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
