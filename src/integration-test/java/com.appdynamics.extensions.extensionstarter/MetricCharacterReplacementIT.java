package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.appdynamics.extensions.extensionstarter.IntegrationTestUtils.initializeMetricAPIService;

public class MetricCharacterReplacementIT {
    private MetricAPIService metricAPIService;

    @Before
    public void setup() {
        metricAPIService = initializeMetricAPIService();
    }

    @Test
    public void testDefaultCharacterReplacement() {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%" +
                    "20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%" +
                    "7CRoot%7CCustom%20Metrics%7CExtension%20Starter%20CI%7CCharacter%20Replacement%7CPipe&time-range-" +
                    "type=BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Character Replacement|Pipe\"", metricName);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }

    @Test
    public void testDefaultCharacterOverrides() {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infras" +
                    "tructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7" +
                    "CCustom%20Metrics%7CExtension%20Starter%20CI%7CCharacter%20Replacement%7CComma%25&time-range-type=" +
                    "BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Character Replacement|Comma%\"", metricName);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }

    @Test
    public void testWhenMultipleReplacementsAreConfiguredForSameCharacterThenLastOneIsUsed () {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infras" +
                    "tructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%" +
                    "7CCustom%20Metrics%7CExtension%20Starter%20CI%7CCharacter%20Replacement%7CComma%25&time-range-type=" +
                    "BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Character Replacement|Comma%\"", metricName);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }


    @Test
    public void testWhenReplacementForNonAsciiCharacterIsPresent() {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infrast" +
                    "ructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7C" +
                    "Custom%20Metrics%7CExtension%20Starter%20CI%7CCharacter%20Replacement%7CComma%25&time-range-type=" +
                    "BEFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Character Replacement|Comma%\"", metricName);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }

    @Test
    public void testWhenReplacementForNonAsciiCharacterIsAbsent() {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infras" +
                    "tructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7C" +
                    "Custom%20Metrics%7CExtension%20Starter%20CI%7CCharacter%20Replacement%7CComma%25&time-range-type=B" +
                    "EFORE_NOW&duration-in-mins=5&output=JSON");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "metricName");
                String metricName = (valueNode == null) ? "" : valueNode.get(0).toString();
                Assert.assertEquals("\"Custom Metrics|Extension Starter CI|Character Replacement|Comma%\"", metricName);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }

    @Test
    public void testWhenInvalidReplacementConfiguredForAsciiCharacter() {

    }
}

