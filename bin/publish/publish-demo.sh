#!/usr/bin/env bash

cd ../../

sbt-nodebug "dist"

rm -rf ./build/publish/databaseflow-demo
rm -rf ./build/publish/databaseflow-1.0.0

unzip ./target/universal/databaseflow-1.0.0.zip -d ./target/universal
mv ./target/universal/database-flow ./build/publish/
mv ./build/publish/database-flow/ ./build/publish/databaseflow-demo

rsync -zrv --delete -e "ssh -i /Users/kyle/.ssh/aws-ec2-key.pem" ./build/publish/databaseflow-demo/* ubuntu@databaseflow.com:~/deploy/databaseflow-demo
