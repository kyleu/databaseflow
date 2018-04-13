#!/usr/bin/env bash

cd ../../

sbt-nodebug "project site" "dist"

rm -rf ./tmp/databaseflow-site
rm -rf ./tmp/databaseflow-site-1.0.0

unzip ./site/target/universal/databaseflow-site-1.1.5.zip -d ./site/target/universal
mv ./site/target/universal/databaseflow-site-1.1.5 ./tmp/
mv ./tmp/databaseflow-site-1.1.5/ ./tmp/databaseflow-site

rsync -zrv --delete ./tmp/databaseflow-site/* kyle@databaseflow.com:~/apps/databaseflow.com
