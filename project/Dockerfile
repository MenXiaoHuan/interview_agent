FROM node:20-alpine AS build

WORKDIR /workspace

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build:h5

FROM nginx:1.27-alpine

ENV PLATFORM_WEB_API_PROXY_TARGET=http://server:8442

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /workspace/dist/build/h5 /usr/share/nginx/html

EXPOSE 80
