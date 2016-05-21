#!/usr/bin/env bash
./switch-personal.sh
cd ../../

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/databaseflow-personal.zip

sbt-nodebug jdkPackager:packageBin
mv ./target/jdkpackager/bundles/databaseflow-1.0.0-MacAppStore.pkg ./build/databaseflow-personal-appstore.pkg
mv ./target/jdkpackager/bundles/databaseflow-1.0.0.dmg ./build/databaseflow-personal.dmg
mv ./target/jdkpackager/bundles/databaseflow-1.0.0.pkg ./build/databaseflow-personal.pkg

cd bin/build
./switch-team.sh

