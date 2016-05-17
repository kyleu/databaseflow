#!/bin/sh
./certbot-auto certonly --manual -d databaseflow.com -d www.databaseflow.com -d demo.databaseflow.com -d dbflow.com -d www.dbflow.com -d demo.dbflow.com
