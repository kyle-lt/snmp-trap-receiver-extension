#!/bin/bash

echo "Unzipping SNMP Trap Receiver to Machine Agent monitors directory."

unzip -o "/Volumes/GoogleDrive/My Drive/Projects/snmp-trap-receiver-extension/target/Snmp-Trap-Receiver-Extension-1.0.zip" -d /opt/appdynamics/machine-agent/monitors

echo "Cleaning up logs in /opt/appdynamics/machine-agent/logs."

rm -f /opt/appdynamics/machine-agent/logs/*

echo "Restarting Machine Agent on MacOS."

MA_PID=$(ps aux | grep machineagent | grep -v grep | awk {'print $2'})

echo "Killing Machine Agent with PID $MA_PID"

kill -9 $MA_PID

echo "Giving the JVM 10s to gracefully shut down."

sleep 10

echo "Starting Machine Agent with command: nohup java -Dmetric.http.listener=true -jar machineagent.jar &"

cd /opt/appdynamics/machine-agent

nohup java -Dmetric.http.listener=true -jar machineagent.jar &

exit 0
