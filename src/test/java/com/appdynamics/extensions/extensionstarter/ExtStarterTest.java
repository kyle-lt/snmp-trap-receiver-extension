/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.extensionstarter.util.Constants.*;

@RunWith(MockitoJUnitRunner.class)
public class ExtStarterTest{

    @Mock
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, String> servers;

    /**
     * Initialize resources common to all tests
     */
    @Before
    public void setup() {
        monitorContextConfiguration = new MonitorContextConfiguration(MONITOR_NAME, DEFAULT_METRIC_PREFIX,
                Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        servers = new HashMap<>();
        servers.put("host", "localhost");
        servers.put("name", "localhost");
    }

    @Test
    public void extStarterMonitorTaskTest() {
        ArgumentCaptor<List> metricCaptor = ArgumentCaptor.forClass(List.class);
        ExtStarterMonitorTask task = new ExtStarterMonitorTask(monitorContextConfiguration, metricWriteHelper, servers);
        task.run();
        Mockito.verify(metricWriteHelper).transformAndPrintMetrics(metricCaptor.capture());
        for (Metric metric : (List<Metric>) metricCaptor.getValue()) {
            Assert.assertEquals(metric.getMetricPath(), "Server|Component:<TIER_ID>|Custom Metrics|Starter|" +
                    "CPUUtilization");
            Assert.assertEquals(metric.getMetricProperties().getAlias(), "Processor Utilization");
            Assert.assertEquals(metric.getMetricValue(), "20");
        }
    }
}