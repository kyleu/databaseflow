#!/usr/bin/env bash

cd ../../

# bin/activator/activator debian:packageBin
# mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow_1.0.0_all.deb

bin/activator/activator docker:publishLocal
docker save --output=./target/databaseflow.docker databaseflow
gzip ./target/databaseflow.docker
mv ./target/databaseflow.docker.gz ./build/databaseflow.docker.gz
