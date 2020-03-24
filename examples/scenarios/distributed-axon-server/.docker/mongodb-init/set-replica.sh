#!/bin/sh
echo "Initializing a local replica set with one member"
mongo --eval "rs.initiate({_id: 'replocal', members: [{_id: 0, host: '127.0.0.1:27017'}] })"
