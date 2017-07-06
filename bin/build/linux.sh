#!/usr/bin/env bash

cd ../../

bin/activator/activator -J-Xmx2048M docker:publishLocal
docker save --output=./target/databaseflow.docker databaseflow
gzip ./target/databaseflow.docker
mv ./target/databaseflow.docker.gz ./build/databaseflow.docker.gz
