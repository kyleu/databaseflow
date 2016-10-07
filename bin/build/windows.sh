#!/usr/bin/env bash

cd ../../

sbt-nodebug assembly

mv "./target/scala-2.11/databaseflow-assembly-1.0.0.jar" "./build/DatabaseFlow.jar"

build/launch4j/launch4j/launch4j ~/Projects/Personal/databaseflow/bin/build/launch4j.xml

cd build/launch4j
zip -r "../DatabaseFlow.zip" . -x "launch4j/*"

