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
        # NOT TESTED!

        # Shortened MIB
        #sudo snmpinform -v 2c -c public 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        # NOT TESTED!

        # OID
        #sudo snmpinform -v 2c -c public 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456
        # NOT TESTED!

# *** Version 3 Security Variables ***

        USERNAME=username
        AUTH_PROTOCOL=MD5
        #AUTH_PROTOCOL=SHA
        AUTH_PASSPHRASE=authpassphrase
        PRIVACY_PROTOCOL=AES
        #PRIVACY_PROTOCOL=DES
        PRIVACY_PASSPHRASE=privacypassphrase
        #LEVEL=authPriv
        #LEVEL=authNoPriv
        #LEVEL=noAuthNoPriv

# *** Version 3 TRAP ***

        # Base of call
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200

        # MIB
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 11111
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0

        # Shortened MIB
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 22222

        # OID
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 33333
        #sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'myTestString!!'

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
        
        # MIB
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0

        # Shortened MIB
        #sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

        # OID
        sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'myTestString!!'


        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=AES 
                # LEVEL=authPriv        N/A
                # LEVEL=authNoPriv      N/A
                # LEVEL=noAuthnoPriv    Not Tested
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=AES
                # LEVEL=authPriv        N/A
                # LEVEL=authNoPriv      N/A
                # LEVEL=noAuthnoPriv    Not Tested
        # AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=DES
                # LEVEL=authPriv        N/A
                # LEVEL=authNoPriv      N/A
                # LEVEL=noAuthnoPriv    Not Tested
        # AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=DES 
                # LEVEL=authPriv        Working!
                # LEVEL=authNoPriv      Working!
                # LEVEL=noAuthnoPriv    Working!


#80:00:13:70:c0:a8:01:0d
#80001370c0a8010d

#80:00:00:02:01:09:84:03:01
#800000020109840301

#Run #1 (noAuthNoPriv)
   # sudo snmptrap -v 3 -e 800000020109840301 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,112 DEBUG SnmpTrapReceiverTask - Engine ID found, using Engine ID: 80:00:00:02:01:09:84:03:01
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,113 DEBUG SnmpTrapReceiverTask - Engine ID Octet String: 80:00:00:02:01:09:84:03:01
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,129 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getLocalEngineID().toString() - byte[] to String: [B@3c1f05ef
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,129 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getUSM().getLocalEngineID() - OctetString: 80:00:13:70:01:ac:16:00:02:7e:05:26:2c
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,130 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getLocalEngineID().toString(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,130 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getUSM().getLocalEngineID(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 14:47:23,130 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using trap sender's snmpEngineIdOctet (provided): UsmUserEntry[userName=username,usmUser=UsmUser[secName=username,authProtocol=1.3.6.1.6.3.10.1.1.2,authPassphrase=authpassphrase,privProtocol=1.3.6.1.6.3.10.1.2.4,privPassphrase=privacypassphrase,localizationEngineID=80:00:00:02:01:09:84:03:01],storageType=nonVolatile]
        # pdu.toString(): TRAP[{contextEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00, contextName=}...

#Run #2 (noAuthNoPriv)
   # sudo snmptrap -v 3 -e 800000020109840301 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,039 DEBUG SnmpTrapReceiverTask - Engine ID found, using Engine ID: 80:00:00:02:01:09:84:03:01
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,040 DEBUG SnmpTrapReceiverTask - Engine ID Octet String: 80:00:00:02:01:09:84:03:01
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,059 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getLocalEngineID().toString() - byte[] to String: [B@46919009
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,059 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getUSM().getLocalEngineID() - OctetString: 80:00:13:70:01:ac:16:00:02:81:da:e1:5f
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,059 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getLocalEngineID().toString(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,059 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getUSM().getLocalEngineID(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 15:08:15,059 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using trap sender's snmpEngineIdOctet (provided): UsmUserEntry[userName=username,usmUser=UsmUser[secName=username,authProtocol=1.3.6.1.6.3.10.1.1.2,authPassphrase=authpassphrase,privProtocol=1.3.6.1.6.3.10.1.2.4,privPassphrase=privacypassphrase,localizationEngineID=80:00:00:02:01:09:84:03:01],storageType=nonVolatile]
        # pdu.toString(): TRAP[{contextEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00, contextName=}...

# Run #3 (noAuthNoPriv)
   # Changed snmpEngineId in config.yml to: "80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00"
   # sudo snmptrap -v 3 -e 80001f88808aced531ed4dd95e00000000 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,693 DEBUG SnmpTrapReceiverTask - Engine ID found, using Engine ID: 80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,694 DEBUG SnmpTrapReceiverTask - Engine ID Octet String: 80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,711 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getLocalEngineID().toString() - byte[] to String: [B@2c8cd06f
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,711 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getUSM().getLocalEngineID() - OctetString: 80:00:13:70:01:ac:16:00:02:74:97:05:9e
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,711 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getLocalEngineID().toString(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,711 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getUSM().getLocalEngineID(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 15:16:34,711 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using trap sender's snmpEngineIdOctet (provided): UsmUserEntry[userName=username,usmUser=UsmUser[secName=username,authProtocol=1.3.6.1.6.3.10.1.1.2,authPassphrase=authpassphrase,privProtocol=1.3.6.1.6.3.10.1.2.4,privPassphrase=privacypassphrase,localizationEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00],storageType=nonVolatile]
        # pdu.toString(): TRAP[{contextEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00, contextName=}...

# Run #3 (noAuthNoPriv)
   # Changed snmpEngineId in config.yml to: "80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00"
   # sudo snmptrap -v 3 -e 80001f88808aced531ed4dd95e00000000 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,149 DEBUG SnmpTrapReceiverTask - Engine ID found, using Engine ID: 80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,149 DEBUG SnmpTrapReceiverTask - Engine ID Octet String: 80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,166 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getLocalEngineID().toString() - byte[] to String: [B@bebc006
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,166 DEBUG SnmpTrapReceiverTask - *** SNMP snmp.getUSM().getLocalEngineID() - OctetString: 80:00:13:70:01:ac:16:00:02:66:79:c8:64
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,166 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getLocalEngineID().toString(): null
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,166 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using snmp.getUSM().getLocalEngineID():           UsmUserEntry[userName=username,usmUser=UsmUser[secName=username,authProtocol=1.3.6.1.6.3.10.1.1.2,authPassphrase=authpassphrase,privProtocol=1.3.6.1.6.3.10.1.2.4,privPassphrase=privacypassphrase,localizationEngineID=80:00:13:70:01:ac:16:00:02:66:79:c8:64],storageType=nonVolatile]
        #[Monitor-Task-Thread1] 06 Jun 2020 15:56:57,166 DEBUG SnmpTrapReceiverTask - *** SNMP.GetUSM.GetUser using trap sender's snmpEngineIdOctet (provided): UsmUserEntry[userName=username,usmUser=UsmUser[secName=username,authProtocol=1.3.6.1.6.3.10.1.1.2,authPassphrase=authpassphrase,privProtocol=1.3.6.1.6.3.10.1.2.4,privPassphrase=privacypassphrase,localizationEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00],storageType=nonVolatile]
        # pdu.toString(): TRAP[{contextEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00, contextName=}, requestID=1106581835, errorStatus=0, errorIndex=0, VBS[1.3.6.1.2.1.1.3.0 = 0:00:00.42; 1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.6.3.1.1.5.1.0]]
        # e.toString(): CommandResponderEvent[securityModel=3, securityLevel=1, maxSizeResponsePDU=65400, pduHandle=PduHandle[1106581835], stateReference=null, pdu=TRAP[{contextEngineID=80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00, contextName=}, requestID=1106581835, errorStatus=0, errorIndex=0, VBS[1.3.6.1.2.1.1.3.0 = 0:00:00.42; 1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.6.3.1.1.5.1.0]], messageProcessingModel=3, securityName=username, processed=false, peerAddress=172.22.0.1/34904, transportMapping=org.snmp4j.transport.DefaultUdpTransportMapping@2ed74308, tmStateReference=null]
        # e.getTransportMapping(): org.snmp4j.transport.DefaultUdpTransportMapping@2ed74308

# snmptrapd
#sudo snmptrap -v 3 -e 800000020109840301 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:162 42 coldStart.0
#sudo snmpinform -v 3 -e 800000020109840301 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:162 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'myTestString!!'

# my receiver
#sudo snmptrap -v 3 -e 800000020109840301 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
#sudo snmptrap -v 3 -E 80001f88808aced531ed4dd95e00000000 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0


# Set #1

#80:00:13:70:01:ac:16:00:02:66:79:c8:64
#8000137001ac1600026679c864

#80:00:13:70:01:ac:16:00:02
#8000137001ac160002

# Set #2

#80:00:1f:88:80:8a:ce:d5:31:ed:4d:d9:5e:00:00:00:00
#80001f88808aced531ed4dd95e00000000

#80:00:1f:88:80:8a:ce:d5:31
#80001f88808aced531

#sudo snmptrap -v 3 -e 8000137001ac1600026679c864 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
#sudo snmptrap -v 3 -e 8000137001ac160002 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
#sudo snmptrap -v 3 -e 80001f88808aced531ed4dd95e00000000 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
#sudo snmptrap -v 3 -e 80001f88808aced531 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 42 coldStart.0
#sudo snmpinform -v 3 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.11.12.13.14.15 s 'myTestString!!'



exit 0
