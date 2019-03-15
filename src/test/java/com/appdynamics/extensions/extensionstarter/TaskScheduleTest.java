package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Satish Muddam
 */

@RunWith(MockitoJUnitRunner.class)
public class TaskScheduleTest {

    
    @Test
    public void testTaskSchedule() throws TaskExecutionException, InterruptedException {

        TestMonitor testMonitor = new TestMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "src/test/resources/conf/config-taskSchedule.yml");

        TestMonitor testMonitorMock = Mockito.spy(testMonitor);
        testMonitorMock.execute(taskArgs, null);

        Thread.sleep(4000l);

        Mockito.verify(testMonitorMock, Mockito.times(1)).doRun(Mockito.any(TasksExecutionServiceProvider.class));

        Mockito.reset(testMonitorMock);

        Thread.sleep((4000l));
        Mockito.verify(testMonitorMock, Mockito.times(1)).doRun(Mockito.any(TasksExecutionServiceProvider.class));
    }


    @Test
    public void testTaskScheduleWithInitialDelay() throws TaskExecutionException, InterruptedException {

        TestMonitor testMonitor = new TestMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "src/test/resources/conf/config-taskScheduleInitDelay.yml");

        TestMonitor testMonitorMock = Mockito.spy(testMonitor);
        testMonitorMock.execute(taskArgs, null);
        testMonitorMock.execute(taskArgs, null);

        Thread.sleep(8000l);
        Mockito.verify(testMonitorMock, Mockito.times(1)).doRun(Mockito.any(TasksExecutionServiceProvider.class));

        Mockito.reset(testMonitorMock);

        Thread.sleep((4000l));
        Mockito.verify(testMonitorMock, Mockito.times(1)).doRun(Mockito.any(TasksExecutionServiceProvider.class));
    }
}


class TestMonitor extends ABaseMonitor {

    @Override
    protected String getDefaultMetricPrefix() {
        return null;
    }

    @Override
    public String getMonitorName() {
        return "Custom Metric|Test";
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        System.out.println("Executing task at " + new Date() + " By " + Thread.currentThread().getName());
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return Lists.newArrayList();
    }
}
