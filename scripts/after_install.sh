#!/bin/bash

sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/cloudcomputing-0.0.1-SNAPSHOT
#sudo rm -rf /opt/tomcat/logs/catalina*
#sudo rm -rf /opt/tomcat/logs/*.log
#sudo rm -rf /opt/tomcat/logs/*.TXT