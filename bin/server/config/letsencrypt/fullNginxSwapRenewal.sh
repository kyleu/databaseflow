#!/bin/bash

cd ~/nginx
sudo cp letsencrypt.default /etc/nginx/sites-available/default
sudo service nginx restart

cd ~/letsencrypt
./renewLetsEncrypt.sh

cd ~/nginx
sudo cp proxy.default /etc/nginx/sites-available/default
sudo service nginx restart
