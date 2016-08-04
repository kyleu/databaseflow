#!/usr/bin/env bash

cd ~/Desktop/build
rm -rf docker
mkdir docker
gzip -k -d databaseflow/build/databaseflow.docker.gz
mv databaseflow/build/databaseflow.docker ./docker
docker load -i ./docker/databaseflow.docker
docker run -it -p 4260:4260 databaseflow:1.0.0
