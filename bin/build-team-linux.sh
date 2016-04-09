#!/usr/bin/env bash
./switch-team.sh

cd ../

bin/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/databaseflow-team.tgz

bin/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow-team_1.0.0_all.deb

