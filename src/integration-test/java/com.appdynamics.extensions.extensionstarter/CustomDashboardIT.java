package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.appdynamics.extensions.util.JsonUtils.getTextValue;

public class CustomDashboardIT {

    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {
        File installDir = new File("src/integration-test/resources/conf/");
        File configFile = new File("src/integration-test/resources/conf/config_ci.yml");
        customDashboardAPIService = IntegrationTestUtils.setUpCustomDashBoardAPIService(configFile, installDir);
    }

    @Test
    public void checkDashboardsUploaded () {
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
            Assert.assertTrue(isDashboardPresent("Extension Starter BTD Dashboard", allDashboardsNode));
        }
    }

    private boolean isDashboardPresent (String dashboardName, JsonNode existingDashboards) {
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
