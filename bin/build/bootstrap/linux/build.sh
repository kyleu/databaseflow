#!/usr/bin/env bash

rsync -av --progress --delete /media/psf/Home/Projects/Personal/databaseflow/ databaseflow/ --exclude 'target*' --exclude '/build' --exclude '.git' --exclude '.idea' --exclude '/tmp' --exclude '/cache' --exclude '/logs'

mkdir -p databaseflow/build/linux

cd databaseflow/bin/build/

./linux.sh

cd ../../..

cp -R databaseflow/build/* /media/psf/Home/Projects/Personal/databaseflow/build
