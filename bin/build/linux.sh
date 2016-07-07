#!/usr/bin/env bash

cd ../../

./bin/build/switch-gui.sh

bin/activator/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/databaseflow-gui.tgz

bin/activator/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow-gui_1.0.0_all.deb

./bin/build/switch-server.sh

bin/activator/activator universal:packageZipTarball
mv ./target/universal/databaseflow-1.0.0.tgz ./build/databaseflow-service.tgz

bin/activator/activator debian:packageBin
mv ./target/databaseflow_1.0.0_all.deb ./build/databaseflow-service_1.0.0_all.deb

