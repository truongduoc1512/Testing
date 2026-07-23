FROM node:20-slim AS base

RUN apt-get update && apt-get install -y \
    chromium \
    fonts-ipafont-gothic \
    fonts-wqy-zenhei \
    fonts-thai-tlwg \
    fonts-kacst \
    fonts-freefont-ttf \
    libxss1 \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true \
    PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium \
    CHROME_EXECUTABLE_PATH=/usr/bin/chromium

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY prisma ./prisma
RUN npx prisma generate

COPY . .

ENV JWT_SECRET=build-time-dummy-jwt-secret-key \
    CACHE_SECRET=build-time-dummy-cache-secret-key \
    DATABASE_URL=mongodb://localhost:27017/dummy-db-build-time

RUN npm run build

EXPOSE 3000

CMD ["npm", "run", "start"]
