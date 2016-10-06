#!/usr/bin/env bash

cd ../../

sbt-nodebug jdkPackager:packageBin
mv "./target/universal/jdkpackager/bundles/Database Flow-1.0.0-MacAppStore.pkg" "./build/DatabaseFlow.pkg"
mv "./target/universal/jdkpackager/bundles/Database Flow-1.0.0.dmg" "./build/DatabaseFlow.dmg"

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/databaseflow.server.zip
