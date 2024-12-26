FROM node:lts as dependencies
WORKDIR /lr-product-shop-app
COPY package.json package-lock.json ./
RUN npm install --frozen-lockfile

FROM node:lts as builder
WORKDIR /lr-product-shop-app
COPY . .

# Create a .env file with the build arguments
ARG NEXT_PUBLIC_BACKEND_URL
RUN echo "NEXT_PUBLIC_BACKEND_URL=$NEXT_PUBLIC_BACKEND_URL" >> .env

COPY --from=dependencies /lr-product-shop-app/node_modules ./node_modules
RUN npm run build

FROM node:lts as runner
WORKDIR /lr-product-shop-app
ENV NODE_ENV production

COPY --from=builder /lr-product-shop-app/public ./public
COPY --from=builder /lr-product-shop-app/package.json ./package.json
COPY --from=builder /lr-product-shop-app/.next ./.next
COPY --from=builder /lr-product-shop-app/node_modules ./node_modules

EXPOSE 3000
CMD ["npm", "start"]