#!/usr/bin/env bash

cd ../../

sbt-nodebug assembly

mv "./target/scala-2.12/databaseflow-assembly-1.0.0.jar" "./build/DatabaseFlow.jar"

build/launch4j/launch4j/launch4j ~/Projects/Personal/databaseflow/bin/build/launch4j.xml

cd build/launch4j
rm "../DatabaseFlow.zip"
zip -r "../DatabaseFlow.zip" . -x "launch4j/*"

