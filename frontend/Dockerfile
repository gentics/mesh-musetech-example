FROM node:8-stretch as builder
RUN mkdir /app
ADD package.json /app
WORKDIR /app
RUN yarn install && yarn cache clean
ADD . /app
RUN yarn run build

FROM nginx
ADD nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/build /usr/share/nginx/html

