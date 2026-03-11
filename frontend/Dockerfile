# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first for better layer caching
COPY package*.json ./
RUN npm ci

# Build static assets
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:1.27-alpine

# Serve the built app and enable SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
