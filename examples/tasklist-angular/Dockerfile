FROM node:8.9 as installer

WORKDIR /opt/build
COPY package.json ./
RUN npm install


FROM node:8.9 as builder
WORKDIR /opt/build
COPY --from=installer /opt/build ./
COPY . ./

RUN npm run build

CMD ["npm"]


