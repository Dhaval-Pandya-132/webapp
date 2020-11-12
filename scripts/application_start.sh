#!/bin/bash


echo "pwd"

cd /home/ubuntu/webapp

pwd

ls -al


sudo mv target/webapp.war /opt/tomcat/webapps/

sudo mv cloudWatchConfig.json /opt/aws/amazon-cloudwatch-agent/etc/

cd /opt/tomcat/webapps/

ls -al

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/cloudWatchConfig.json -s

sudo /opt/tomcat/bin/startup.sh