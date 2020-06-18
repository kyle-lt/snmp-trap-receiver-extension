package com.appdynamics.extensions.snmptrapreceiverextension.events;

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SnmpTrapReceiverEventsManager {

    private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiverEventsManager.class);
    private EventsServiceDataManager eventsServiceDataManager;

    public SnmpTrapReceiverEventsManager(EventsServiceDataManager eventsServiceDataManager) {
    	
    	logger.debug("Instantiating the Analytics Events Manager");
        this.eventsServiceDataManager = eventsServiceDataManager;
    }

    public void createSchema() throws Exception {
    	logger.debug("Creating Analytics Schema");
    	String schema = "{\n" + 
    			"  \"schema\":\n" + 
    			"  {\n" + 
    			"    \"method\":\"string\",\n" + 
    			"    \"version\":\"string\",\n" + 
    			"    \"kyle\":\"string\"\n" + 
    			"  }\n" + 
    			"}";
        //eventsServiceDataManager.createSchema("SnmpTrapReceiver", FileUtils.readFileToString(new File("src/main/resources/eventsservice/createSchema.json")));
    	eventsServiceDataManager.createSchema("SnmpTrapReceiver", schema);
    }

    public void updateSchema() throws Exception {
        eventsServiceDataManager.updateSchema("SnmpTrapReceiver", FileUtils.readFileToString(new File("src/main/" +
                "resources/eventsservice/updateSchema.json")));
    }

    public void deleteSchema() {
        eventsServiceDataManager.deleteSchema("SnmpTrapReceiver");
    }

    public void publishEvents(String analyticsEventString) {
        //eventsServiceDataManager.publishEvents("SnmpTrapReceiver", generateEventsFromFile(new File("src/main/" + "resources/eventsservice/publishEvents.json")));
    	List<String> list = new ArrayList<>();
    	list.add(analyticsEventString);
    	logger.debug("Publishing Event.");
    	logger.debug("list = " + list);
    	
    	logger.debug("Recreating what publish is doing...");
    	String batchBody = list.stream().collect(Collectors.joining(",", "[", "]"));
    	logger.debug("Here is the content of batchBody: " + batchBody);
    	
    	eventsServiceDataManager.publishEvents("SnmpTrapReceiver", list);
    }

    public String queryEvents() {
        return eventsServiceDataManager.querySchema("SELECT appName FROM BTDSCHEMA");
    }
    
/*
    private List<String> generateEventsFromFile(File eventsFromFile) {
        List<String> eventsToBePublishedForSchema = Lists.newArrayList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(eventsFromFile);
            for (JsonNode node : arrayNode) {
            	logger.debug("Here is what is being passed to publishEvents:");
            	logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
                eventsToBePublishedForSchema.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
            }
        } catch (Exception ex) {
            logger.error("Error encountered while generating events from file: {}", eventsFromFile.getAbsolutePath(), ex);
        }
        return eventsToBePublishedForSchema;
    }
    */
}
