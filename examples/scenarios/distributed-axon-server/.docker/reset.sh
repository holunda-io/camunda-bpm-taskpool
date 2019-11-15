#!/bin/sh

docker rm postgres
docker rm axonserver
docker rm mongodb

docker volume rm postgres-data
docker volume rm axonserver-data
docker volume rm mongo-data

docker network rm taskpool-net
