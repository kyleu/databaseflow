#!/usr/bin/env bash

cd ../../

sbt-nodebug "dist"

rm -rf ./tmp/databaseflow-demo
rm -rf ./tmp/databaseflow-1.0.0

unzip ./target/universal/databaseflow-1.0.0.zip -d ./target/universal
mv ./target/universal/database-flow ./tmp/
mv ./tmp/database-flow/ ./tmp/databaseflow-demo

rsync -zrv --delete -e "ssh -i /Users/kyle/.ssh/aws-ec2-key.pem" ./tmp/databaseflow-demo/* ubuntu@databaseflow.com:~/deploy/databaseflow-demo
