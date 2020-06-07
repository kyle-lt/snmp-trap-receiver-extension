#!/bin/bash

# Versions 1 and 2c use community string and always go over UDP (as far as I can tell)
   # Everything is pretty damn easy
# For v3 - Some examples below send the security engine ID (using -e flag), but this is generally not required, but here's the rub:
# For v3 Traps to work using auth and priv (encryption), auto engine ID detection cannot be done.  The sender needs to 
# send the receiver the sender's engine ID (along with the user), and these values have to be in the receiver's DB beforehand.
# The question is, how can this be accomplished?!?!?!? (gracefully)
# *** So, for now, for v3, it's required to send Traps as noAuthNoPriv ***
# Otherwise, we need to configure the extension with the local engine ID.

# *** Version 1 TRAP ***

        # MIB
        #sudo snmptrap -v 1 -c public 127.0.0.1:16200 NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification "" 6 17 "" netSnmpExampleHeartbeatRate i 123456
        # WORKING!

        # Shortened MIB
        #sudo snmptrap -v 1 -c public 127.0.0.1:16200 netSnmpExampleHeartbeatNotification "" 6 17 "" netSnmpExampleHeartbeatRate i 123456
        # WORKING!

        # OID
        #sudo snmptrap -v 1 -c public 127.0.0.1:16200 '1.2.3.4.5.6' '192.193.194.195' 6 99 '55' 1.11.12.13.14.15  s "teststring"
        # WORKING!

# *** Version 2c TRAP ***

        # MIB
        #sudo snmptrap -v 2c -c public 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        # WORKING!

        # Shortened MIB
        #sudo snmptrap -v 2c -c public 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        # WORKING!

        # OID
        #sudo snmptrap -v 2c -c public 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456
        # WORKING!

# *** Version 2c INFORM ***

        # MIB
        #sudo snmpinform -v 2c -c public 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        # Working!

        # Shortened MIB
        #sudo snmpinform -v 2c -c public 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        # Working!

        # OID
        #sudo snmpinform -v 2c -c public 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456
        # Working!

# *** Version 3 Security Variables ***

        # Only 1 AUTH_PROTOCOL, PRIVACY_PROTOCOL, and LEVEL can be selected at once
        USERNAME=username
        AUTH_PROTOCOL=MD5
        #AUTH_PROTOCOL=SHA
        AUTH_PASSPHRASE=authpassphrase
        PRIVACY_PROTOCOL=AES
        #PRIVACY_PROTOCOL=DES
        PRIVACY_PASSPHRASE=privacypassphrase
        #LEVEL=authPriv
        #LEVEL=authNoPriv
        LEVEL=noAuthNoPriv

# *** Version 3 TRAP ***

        # Base of call
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200
        SNMP_TRAP_BASE_CALL="sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200"

        # MIB
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 11111
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0

        # Shortened MIB
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 22222

        # OID
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 33333
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'myTestString!!'
        SNMP_TRAP_CALL_DETAILS="'' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'v3Trap'"
        SNMP_TRAP="$SNMP_TRAP_BASE_CALL $SNMP_TRAP_CALL_DETAILS"
        echo "Calling SNMP_TRAP: " $SNMP_TRAP
        $($SNMP_TRAP)
        sleep 1
        RESULT=$(docker exec machine cat /opt/appdynamics/logs/machine-agent.log | grep "SnmpTrapReceiverTask.*Value: 'v3Trap'")
        if [ -z "$RESULT" ]; then
                echo "Successful log not found, check logs!"
        else
                echo "Successful log(s) found!"
                echo "Here are the log entries, be sure to validate the timestamp(s)!"
                echo "$RESULT"
        fi

        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=AES 
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=AES
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=DES
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=DES 
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!

# *** Version 3 INFORM ***

        # Base of call
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200
        SNMP_INFORM_BASE_CALL="sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200"
        #echo "SNMP_INFORM_BASE_CALL = " $SNMP_INFORM_BASE_CALL
        
        # MIB
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0

        # Shortened MIB
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

        # OID
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'v3Inform'
        SNMP_INFORM_CALL_DETAILS="'' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'v3Inform'"
        SNMP_INFORM="$SNMP_INFORM_BASE_CALL $SNMP_INFORM_CALL_DETAILS"
        echo "Calling SNMP_INFORM: " $SNMP_INFORM        
        $($SNMP_INFORM)
        RESULT="$(docker exec machine cat /opt/appdynamics/logs/machine-agent.log | grep "SnmpTrapReceiverTask.*Value: 'v3Inform'")"
        if [ -z "$RESULT" ]; then
                echo "Successful log not found, check logs!"
        else
                echo "Successful log(s) found!"
                echo "Here are the log entries, be sure to validate the timestamp(s)!"
                echo "$RESULT"
        fi

        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=AES 
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=AES
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=DES
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!
        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=DES 
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!




exit 0
