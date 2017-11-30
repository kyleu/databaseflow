#!/usr/bin/env bash

cd ../../

sbt-nodebug assembly

mv "./target/scala-2.12/databaseflow-assembly-1.1.1.jar" "./build/DatabaseFlow.jar"
