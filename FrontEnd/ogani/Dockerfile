# Build stage
FROM node:18-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Build the application
RUN npm run build -- --configuration production --optimization=true --build-optimizer=true --aot=true

# Runtime stage (Nginx)
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built application from build stage
COPY --from=build /app/dist/ogani .

# Optional custom nginx config (already declared elsewhere)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 5361

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
