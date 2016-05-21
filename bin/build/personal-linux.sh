#!/usr/bin/env bash
./switch-personal.sh
cd ../

bin/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/databaseflow-personal.tgz

bin/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow-personal_1.0.0_all.deb

cd bin/build
./switch-team.sh

