#!/usr/bin/env bash
./switch-solo.sh
cd ../../

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/databaseflow-solo.zip

sbt-nodebug jdkPackager:packageBin
mv ./target/jdkpackager/bundles/databaseflow-1.0.0-MacAppStore.pkg ./build/databaseflow-solo-appstore.pkg
mv ./target/jdkpackager/bundles/databaseflow-1.0.0.dmg ./build/databaseflow-solo.dmg
mv ./target/jdkpackager/bundles/databaseflow-1.0.0.pkg ./build/databaseflow-solo.pkg

cd bin/build
./switch-team.sh

