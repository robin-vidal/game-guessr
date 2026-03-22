FROM node:22-alpine AS builder

WORKDIR /app

ARG NOCLIP_FRONTEND_URL=http://localhost:8000
ENV NOCLIP_FRONTEND_URL=$NOCLIP_FRONTEND_URL

COPY package.json package-lock.json ./
RUN corepack enable npm && npm install
RUN npm ci --platform=linux --libc=musl

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]