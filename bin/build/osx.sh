#!/usr/bin/env bash

cd ../../

# sbt-nodebug jdkPackager:packageBin
# mv ./target/universal/jdkpackager/bundles/databaseflow-1.0.0-MacAppStore.pkg ./build/osx/databaseflow-appstore.pkg
# mv ./target/universal/jdkpackager/bundles/databaseflow-1.0.0.dmg ./build/osx/databaseflow.dmg

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/osx/databaseflow.zip
