#!/bin/bash

sudo systemctl stop tomcat
cd /opt/tomcat/webapps
sudo rm -rf cloudcomputing-0.0.1-SNAPSHOT.war