# Етап 1: Компилиране на фронтенда
FROM node:20.19 AS builder
WORKDIR /app

# 1. Копиране на package.json и package-lock.json от папката frontend
COPY frontend/package*.json ./

# 2. Инсталиране на депендънситата
RUN npm install


# 3. Копиране на ВСИЧКИ файлове от фронтенд папката (включително src, index.html и т.н.)
COPY frontend/ ./

# 4. Компилиране на проекта
RUN npm run build

RUN ls -la dist/
RUN ls -la dist/*


# Етап 2: Сервиране чрез Nginx
FROM nginx:alpine
# Копиране на готовия билд към Nginx
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
