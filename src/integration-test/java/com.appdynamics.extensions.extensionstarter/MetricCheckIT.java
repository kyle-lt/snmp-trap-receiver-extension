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
       // http://ec2-54-202-144-212.us-west-2.compute.amazonaws.com:8090/controller/rest/applications/Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7CProcessor%20Utilization&time-range-type=BEFORE_NOW&duration-in-mins=1440

       /* JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7CProcessor%20Utilization&time-range-type=BEFORE_NOW&duration-in-mins=1440&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        if (jsonNode != null) {
            JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
            int cpuUtilization = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            Assert.assertEquals(cpuUtilization, 20);
        }*/
    }

    @Test
    public void testDerivedMetricUpload() {}
}


