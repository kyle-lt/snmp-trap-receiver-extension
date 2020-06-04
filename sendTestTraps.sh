#!/bin/bash

# Versions 1 and 2c use community string and always go over UDP (as far as I can tell)

# Version 1
sudo snmptrap -v 2c -c public 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456

# Version 2c

# MIB
#snmptrap -v 2c -c public localhost '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

# Shortened MIB
#snmptrap -v 2c -c public localhost '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

# OID
#snmptrap -v 2c -c public localhost '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456

# Version 3

USERNAME=username
AUTH_PROTOCOL=MD5
#AUTH_PROTOCOL=SHA
AUTH_PASSPHRASE=authpassphrase
#PRIVACY_PROTOCOL=AES
PRIVACY_PROTOCOL=DES
PRIVACY_PASSPHRASE=privacypassphrase
#LEVEL=authPriv
LEVEL=authNoPriv
#LEVEL=noAuthNoPriv

# MIB
#sudo snmptrap -v 3 -e 0x090807060504030201 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' NET-SNMP-EXAMPLES-MIB::netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

# AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=AES 
        # LEVEL=authPriv        Working!
        # LEVEL=authNoPriv      Not Tested
        # LEVEL=noAuthnoPriv    Not Tested
# AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=AES
        # LEVEL=authPriv        Not Tested
        # LEVEL=authNoPriv      Not Tested
        # LEVEL=noAuthnoPriv    Not Tested
# AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=DES
        # LEVEL=authPriv        Not Tested
        # LEVEL=authNoPriv      Not Tested
        # LEVEL=noAuthnoPriv    Not Tested
# AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=DES 
        # LEVEL=authPriv        NOT Working! DES works with SHA, but not with MD5, why?
        # LEVEL=authNoPriv      Working!
        # LEVEL=noAuthnoPriv    Working!

# Shortened MIB
#snmptrap -v 3 -e 0x090807060504030201 -u the_user_name -a SHA -A the_SHA_string -x AES -X the_AES_string localhost '' netSnmpExampleHeartbeatNotification netSnmpExampleHeartbeatRate i 123456

# OID

#sudo snmptrap -v 3 -e 0x090807060504030201 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456
# Note that changing the auth or encryption requires restarting the SNMP Trap listener!
# AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=AES 
	# LEVEL=authPriv	Working!
	# LEVEL=authNoPriv	Working!
	# LEVEL=noAuthnoPriv	Working!
# AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=AES
        # LEVEL=authPriv        Working!
        # LEVEL=authNoPriv      Working!
        # LEVEL=noAuthnoPriv    Working!
# AUTH_PROTOCOL=SHA, PRIVACY_PROTOCOL=DES
        # LEVEL=authPriv        Working!
        # LEVEL=authNoPriv      Working!
        # LEVEL=noAuthnoPriv    Working!
# AUTH_PROTOCOL=MD5, PRIVACY_PROTOCOL=DES 
        # LEVEL=authPriv        NOT Working! DES works with SHA, but not with MD5, why?
        # LEVEL=authNoPriv      Working!
        # LEVEL=noAuthnoPriv    Working!

#sudo snmpinform -v 3 -e 0x090807060504030201 -l $LEVEL -u $USERNAME -a $AUTH_PROTOCOL -A $AUTH_PASSPHRASE -x $PRIVACY_PROTOCOL -X $PRIVACY_PASSPHRASE 127.0.0.1:16200 '' 1.3.6.1.4.1.8072.2.3.0.1 1.3.6.1.4.1.8072.2.3.2.1 i 123456
# Not working

exit 0
