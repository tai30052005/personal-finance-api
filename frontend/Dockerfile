# ============================================================
#  Multi-stage build cho frontend React:
#    Stage 1 (node)  : cài deps + build ra thư mục dist/ (HTML/CSS/JS tĩnh)
#    Stage 2 (nginx) : phục vụ file tĩnh + proxy /api sang backend
# ============================================================

# ---------- STAGE 1: BUILD ----------
FROM node:22-alpine AS build
WORKDIR /app

# Copy file khai báo dependency trước để tận dụng cache (như mẹo ở backend)
COPY package*.json ./
RUN npm ci

# Copy mã nguồn và build (Vite dùng .env.production -> API base rỗng -> gọi /api)
COPY . .
RUN npm run build

# ---------- STAGE 2: SERVE ----------
FROM nginx:alpine
# Lấy thư mục build tĩnh sang webroot của Nginx
COPY --from=build /app/dist /usr/share/nginx/html
# Cấu hình Nginx (SPA + proxy /api)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
