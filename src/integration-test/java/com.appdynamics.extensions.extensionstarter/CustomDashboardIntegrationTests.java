package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

public class CustomDashboardIntegrationTests {

    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {

        customDashboardAPIService = IntegrationTestUtils.initializeCustomDashboardAPIService();
    }

    @Test
    public void testWhetherConfiguredDashboardIsUploadedToController() {
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
            Assert.assertTrue(isDashboardPresent("Extension Starter BTD Dashboard", allDashboardsNode));
        }
    }

    private boolean isDashboardPresent(String dashboardName, JsonNode existingDashboards) {
        if (existingDashboards != null) {
            for (JsonNode existingDashboard : existingDashboards) {
                if (dashboardName.equals(getTextValue(existingDashboard.get("name")))) {
                    return true;
                }
            }
        }
        return false;
    }
}