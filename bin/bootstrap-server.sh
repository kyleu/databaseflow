#!/usr/bin/env bash

cd
mkdir deploy

sudo nano /etc/hostname

sudo apt-get update
sudo apt-get upgrade

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer

sudo apt-get install postgresql-client
sudo apt-get install postgresql postgresql-contrib

sudo -u postgres psql postgres
# \password postgres

sudo -u postgres createdb databaseflow

sudo -u postgres createuser --superuser databaseflow
sudo -u postgres psql postgres
# \password databaseflow

sudo -u postgres nano /etc/postgresql/9.3/main/postgresql.conf
# listen_addresses = '*'

sudo -u postgres nano /etc/postgresql/9.3/main/pg_hba.conf
# host     all            all             X.X.X.X/32         md5
