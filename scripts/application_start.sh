#!/bin/bash


echo "pwd"

pwd

ls -al


sudo mv target/cloudcomputing-0.0.1-SNAPSHOT.war /opt/tomcat/webapps/

cd /opt/tomcat/webapps/

ls -al

sudo systemctl start tomcat