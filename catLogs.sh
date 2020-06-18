#!/bin/bash

if [ -f /tmp/machine-agent.log ]
then
	rm -f /tmp/machine-agent.log
fi

docker cp machine:/opt/appdynamics/logs/machine-agent.log /tmp

cat /tmp/machine-agent.log | grep SnmpTrapReceiverTask

cat /tmp/machine-agent.log | grep SnmpTrapReceiverEventsManager

cat /tmp/machine-agent.log | grep EventsServiceDataManager

exit 0
