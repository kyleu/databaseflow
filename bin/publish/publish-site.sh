#!/usr/bin/env bash

cd ../../

sbt-nodebug "project site" "dist"

rm -rf ./build/publish/databaseflow-site
rm -rf ./build/publish/databaseflow-site-1.0.0

unzip ./site/target/universal/databaseflow-site-1.0.0.zip -d ./site/target/universal
mv ./site/target/universal/databaseflow-site-1.0.0 ./build/publish/
mv ./build/publish/databaseflow-site-1.0.0/ ./build/publish/databaseflow-site
