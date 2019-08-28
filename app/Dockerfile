FROM node:8-stretch

ADD . /app

WORKDIR /app

RUN ls -la

RUN npm install

EXPOSE 3000

CMD ["npm", "start"]
