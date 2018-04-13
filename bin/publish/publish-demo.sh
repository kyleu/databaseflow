#!/usr/bin/env bash

cd ../../

sbt-nodebug "dist"

rm -rf ./tmp/databaseflow-demo
rm -rf ./tmp/databaseflow-1.1.5

unzip ./target/universal/databaseflow-1.1.5.zip -d ./target/universal/databaseflow
mv ./target/universal/databaseflow ./tmp/
mv ./tmp/databaseflow/ ./tmp/databaseflow-demo

rsync -zrv --delete ./tmp/databaseflow-demo/* kyle@databaseflow.com:~/apps/demo.databaseflow.com
