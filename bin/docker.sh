#!/usr/bin/env bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir=${dir}
cd $project_dir/..

sbt docker:publishLocal
docker save --output=./target/databaseflow.docker databaseflow
gzip ./target/databaseflow.docker
