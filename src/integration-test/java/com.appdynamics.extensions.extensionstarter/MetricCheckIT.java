package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class MetricCheckIT {
    MetricAPIService metricAPIService;

    @Before
    public void setup() {
        File installDir = new File("src/integration-test/resources/conf/");
        File configFile = new File("src/integration-test/resources/conf/config_ci.yml");
        metricAPIService = IntegrationTestUtils.setUpMetricAPIService(configFile, installDir);
    }

    @Test
    public void testMetricUpload() {

    }
}


