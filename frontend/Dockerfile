FROM node:8-stretch as builder
RUN mkdir /app
# 1. Only add package json and install deps. 
# This way we can cache those in a build layer
ADD package.json /app
WORKDIR /app
RUN npm install

# 2. Now add the sources and build the app
ADD . /app
RUN npm run build

# Build the final image
FROM nginx
ADD nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/build /usr/share/nginx/html

