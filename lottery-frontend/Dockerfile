# Stage 1: build dell'app Vue con Vite
FROM node:20-alpine AS build
WORKDIR /app

ARG VITE_API_BASE_URL
ARG VITE_API_USERNAME
ARG VITE_API_PASSWORD
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_API_USERNAME=$VITE_API_USERNAME
ENV VITE_API_PASSWORD=$VITE_API_PASSWORD

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: servire i file statici con nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80