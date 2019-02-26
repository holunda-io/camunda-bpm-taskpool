FROM node:10.15 as installer

WORKDIR /opt/build
COPY package.json ./
RUN npm install


FROM node:10.15 as builder
WORKDIR /opt/build
COPY --from=installer /opt/build ./

COPY . ./
RUN npm run build

CMD ["npm"]

FROM node:10.15 as runner

WORKDIR /opt/build
COPY --from=builder /opt/build ./

VOLUME /opt/build/src

EXPOSE 4201
