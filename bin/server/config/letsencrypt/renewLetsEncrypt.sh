#!/bin/bash

cd /opt/letsencrypt
./letsencrypt-auto certonly -a webroot --webroot-path=/usr/share/nginx/html -d databaseflow.com -d www.databaseflow.com -d demo.databaseflow.com -d dbflow.com -d www.dbflow.com -d demo.dbflow.com

cd /home/ubuntu/letsencrypt
sudo cp -LrR /etc/letsencrypt/live/databaseflow.com .
sudo chown -R ubuntu:ubuntu databaseflow.com
