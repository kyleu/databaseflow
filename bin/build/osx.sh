#!/usr/bin/env bash

cd ../../

sbt-nodebug jdkPackager:packageBin
mv "./target/universal/jdkpackager/bundles/Database Flow-1.0.0-MacAppStore.pkg" "./build/Database Flow.pkg"
mv "./target/universal/jdkpackager/bundles/Database Flow-1.0.0.dmg" "./build/Database Flow.dmg"

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/databaseflow.zip
