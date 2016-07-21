#!/usr/bin/env bash

cd ../../

./bin/build/switch-service.sh

bin/activator/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/linux/databaseflow.tgz

bin/activator/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/linux/databaseflow_1.0.0_all.deb

bin/activator/activator docker:publishLocal
docker save --output=./target/databaseflow.docker databaseflow
gzip ./target/databaseflow.docker
mv ./target/databaseflow.docker.gz ./build/linux/databaseflow.docker.gz
