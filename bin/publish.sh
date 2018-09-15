#!/usr/bin/env bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir=${dir}
cd $project_dir/..

sbt dist

rm -rf ./tmp/databaseflow

unzip ./target/universal/databaseflow.zip -d ./target/universal/databaseflow
mv ./target/universal/databaseflow ./tmp/

rsync -zrv --delete ./tmp/databaseflow/* kyle@databaseflow.com:~/apps/demo.databaseflow.com
