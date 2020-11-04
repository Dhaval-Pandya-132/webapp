#!/bin/bash

sudo systemctl stop tomcat

cd /opt/tomcat/webapps

sudo rm -rf webapp.war