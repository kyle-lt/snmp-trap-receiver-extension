#!/bin/bash

docker exec machine tail -f /opt/appdynamics/logs/machine-agent.log | grep SnmpTrapReceiverTask

exit 0
