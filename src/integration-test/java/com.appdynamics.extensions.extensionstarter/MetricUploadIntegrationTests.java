package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.appdynamics.extensions.extensionstarter.IntegrationTestUtils.initializeMetricAPIService;

public class MetricUploadIntegrationTests {

    private MetricAPIService metricAPIService;

    @Before
    public void setup() {
        metricAPIService = initializeMetricAPIService();
    }

    @Test
    public void testMetricUpload() {
        if(metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("","Server%20&%20Infrastructure%20Monitoring/" +
                    "metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtensi" +
                    "on%20Starter%20CI%7CIncomingRequests&time-range-type=BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                int cpuUtilization = (valueNode == null) ? 0 : valueNode.get(0).asInt();
                Assert.assertEquals(cpuUtilization, 20);
            }
        }
        else {
            Assert.fail("Failed to connect to the Controller API");
        }
    }

    @Test
    public void testMetricUploadWithAliasMultiplier() {
        if(metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("","Server%20&%20Infrastructure%20Monitoring/" +
                    "metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtens" +
                    "ion%20Starter%20CI%7COutgoing%20Requests&time-range-type=BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                int metricValue = (valueNode == null) ? 0 : valueNode.get(0).asInt();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Outgoing Requests\"", metricName);
                Assert.assertEquals(100, metricValue);
            }
        }
        else {
            Assert.fail("Failed to connect to the Controller API");
        }
    }
}