FROM appdynamics/machine-agent-analytics:latest AS MA

RUN apt-get update && \ 
    apt-get install -y unzip
#    apt-get install -y curl && \
#    apt-get install -y vim && \
#    apt-get install -y net-tools && \
#    apt-get install -y netcat

ADD target/Snmp-Trap-Receiver-Extension-*.zip /opt/appdynamics/monitors
RUN unzip -q "/opt/appdynamics/monitors/Snmp-Trap-Receiver-Extension-*.zip" -d /opt/appdynamics/monitors
RUN find /opt/appdynamics/monitors/ -name '*.zip' -delete

# Turn on DEBUG LOGGING
RUN sed -i "s|<Logger name=\"com.singularity\" level=\"info\" additivity=\"false\">|<Logger name=\"com.singularity\" level=\"debug\" additivity=\"false\">|" /opt/appdynamics/conf/logging/log4j.xml
RUN sed -i "s|<Logger name=\"com.appdynamics\" level=\"info\" additivity=\"false\">|<Logger name=\"com.appdynamics\" level=\"debug\" additivity=\"false\">|" /opt/appdynamics/conf/logging/log4j.xml

CMD ["sh", "-c", "java ${MACHINE_AGENT_PROPERTIES} -jar /opt/appdynamics/machineagent.jar"]
