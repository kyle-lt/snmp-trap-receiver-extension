package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class MetricCheckIT {
    private MetricAPIService metricAPIService;

    @Before
    public void setup() {
        File installDir = new File("src/integration-test/resources/conf/");
        File configFile = new File("src/integration-test/resources/conf/config_ci.yml");
        metricAPIService = IntegrationTestUtils.setUpMetricAPIService(configFile, installDir);
    }

    @Test
    public void testMetricUpload() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7CIncomingRequests&time-range-type=BEFORE_NOW&duration-in-mins=5&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            int cpuUtilization = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals(cpuUtilization, 20);
        }
    }

    @Test
    public void testMetricUploadWithAliasMultiplier() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7COutgoing%20Requests&time-range-type=BEFORE_NOW&duration-in-mins=5&output=JSON");
            Assert.assertNotNull("Cannot connect to controller API", jsonNode);
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();

                valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Outgoing Requests\"", metricName);
                Assert.assertEquals(100, metricValue);
            }
        }
    }


    @Test
    public void testDerivedMetricUpload() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7CTotal%20Number%20of%20Requests&time-range-type=BEFORE_NOW&duration-in-mins=5&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            int totalNumOfRequests = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals(totalNumOfRequests, 120);
        }
    }
}