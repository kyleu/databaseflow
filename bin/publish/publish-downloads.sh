#!/usr/bin/env bash

cd ../../

rsync -zrv -e "ssh -i /Users/kyle/.ssh/aws-ec2-key.pem" ./build/* ubuntu@databaseflow.com:~/deploy/databaseflow-downloads --exclude '/jwrapper'
