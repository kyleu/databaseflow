#!/usr/bin/env bash

cd ../../

sbt-nodebug "project site" "dist"

rm -rf ./tmp/databaseflow-site
rm -rf ./tmp/databaseflow-site-1.0.0

unzip ./site/target/universal/databaseflow-site-1.1.1.zip -d ./site/target/universal
mv ./site/target/universal/databaseflow-site-1.1.1 ./tmp/
mv ./tmp/databaseflow-site-1.1.1/ ./tmp/databaseflow-site

rsync -zrv --delete -e "ssh -i /Users/kyle/.ssh/aws-ec2-key.pem" ./tmp/databaseflow-site/* ubuntu@databaseflow.com:~/deploy/databaseflow-site
