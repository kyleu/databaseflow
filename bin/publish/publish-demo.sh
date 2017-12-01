#!/usr/bin/env bash

cd ../../

sbt-nodebug "dist"

rm -rf ./tmp/databaseflow-demo
rm -rf ./tmp/databaseflow-1.1.1

unzip ./target/universal/databaseflow-1.1.1.zip -d ./target/universal/databaseflow
mv ./target/universal/databaseflow ./tmp/
mv ./tmp/databaseflow/ ./tmp/databaseflow-demo

rsync -zrv --delete -e "ssh -i /Users/kyle/.ssh/aws-ec2-key.pem" ./tmp/databaseflow-demo/* ubuntu@databaseflow.com:~/deploy/databaseflow-demo
