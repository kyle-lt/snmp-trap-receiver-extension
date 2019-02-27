package com.appdynamics.extensions.extensionstarter.events;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.extensionstarter.ExtStarterMonitorTask;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class EventsManager {

    private static final Logger logger = LoggerFactory.getLogger(EventsManager.class);

    public void generateAndPublishEvents(EventsServiceDataManager eventsServiceDataManager) throws Exception {
        eventsServiceDataManager.createSchema("BTDSchema", FileUtils.readFileToString(new File("src/" +
                "integration-test/resources/eventsservice/createSchema.json")));
        eventsServiceDataManager.updateSchema("BTDSchema", FileUtils.readFileToString(new File("src/integration-test/" +
                "resources/eventsservice/updateSchema.json")));
        eventsServiceDataManager.publishEvents("schema1", generateEventsFromFile(new File("src/test/" +
                "resources/eventsservice/publishEvents.json")));
    }

    private List<String> generateEventsFromFile(File eventsFromFile) {
        List<String> eventsToBePublishedForSchema = Lists.newArrayList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(eventsFromFile);
            for (JsonNode node : arrayNode) {
                eventsToBePublishedForSchema.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
            }
        } catch (Exception ex) {
            logger.error("Error encountered while generating events from file: {}", eventsFromFile.getAbsolutePath(), ex);
        }
        return eventsToBePublishedForSchema;
    }
}
