#!/bin/bash


echo "pwd"

cd /home/ubuntu/webapp

pwd

ls -al


sudo mv target/webapp.war /opt/tomcat/webapps/

cd /opt/tomcat/webapps/

ls -al

sudo /opt/tomcat/bin/startup.sh