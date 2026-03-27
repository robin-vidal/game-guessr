FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN corepack enable pnpm && pnpm install
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
COPY docker/config.js.template /etc/nginx/templates/config.js.template

ARG NOCLIP_FRONTEND_URL=http://localhost:8000
ENV VITE_NOCLIP_FRONTEND_URL=$NOCLIP_FRONTEND_URL

CMD ["/bin/sh", "-c", \
  "envsubst < /etc/nginx/templates/config.js.template > /usr/share/nginx/html/config.js && \
   nginx -g 'daemon off;'"]

EXPOSE 5173