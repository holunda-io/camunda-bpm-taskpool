#!/bin/sh

docker volume create postgres-data
docker volume create axonserver-data
docker volume create mongo-data

docker network create taskpool-net

