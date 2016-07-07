#!/usr/bin/env bash

cd ../../

./bin/build/switch-gui.sh

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/osx/databaseflow-gui.zip

sbt-nodebug jdkPackager:packageBin
mv ./target/universal/jdkpackager/bundles/databaseflow-1.0.0-MacAppStore.pkg ./build/osx/databaseflow-gui-appstore.pkg
mv ./target/universal/jdkpackager/bundles/databaseflow-1.0.0.dmg ./build/osx/databaseflow-gui.dmg
mv ./target/universal/jdkpackager/bundles/databaseflow-1.0.0.pkg ./build/osx/databaseflow-gui.pkg

./bin/build/switch-server.sh

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/osx/databaseflow-service.zip

sbt-nodebug universal:packageOsxDmg
mv ./target/universal/databaseflow-1.0.0.dmg ./build/osx/databaseflow-service.dmg

