#!/usr/bin/env bash
./switch-team.sh

cd ../../

sbt-nodebug universal:packageBin
mv ./target/universal/databaseflow-1.0.0.zip ./build/databaseflow-team.zip

sbt-nodebug universal:packageOsxDmg
mv ./target/universal/databaseflow-1.0.0.dmg ./build/databaseflow-team.dmg

