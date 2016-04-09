#!/usr/bin/env bash
./switch-solo.sh
cd ../

bin/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/databaseflow-solo.tgz

bin/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow-solo_1.0.0_all.deb

cd bin
./switch-team.sh

