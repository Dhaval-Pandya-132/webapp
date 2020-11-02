#!/bin/bash

cd /home/ubuntu/webapp

cd ..

sudo chmod uo+rwx webapp

cd webapp/

mvn package

mv target/cloudcomputing-0.0.1-SNAPSHOT.war /opt/tomcat/webapps/

cd /opt/tomcat/webapps/

ls -al

sudo systemctl start tomcat