/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.snmptrapreceiverextension;

/**
 * Created by Kyle Tully on 05/26/2020.
 */

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
//import com.appdynamics.extensions.snmptrapreceiverextension.events.SnmpTrapReceiverEventsManager;
import com.appdynamics.extensions.util.AssertUtils;
//import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.*;

//import java.io.OutputStreamWriter;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This class will be the main implementation for the extension, the entry point
 * for this class is
 * {@code doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider)}
 *
 * {@code ABaseMonitor} class takes care of all the boiler plate code required
 * for "ExtensionMonitor" like initializing {@code MonitorContexConfiguration},
 * setting the config file from monitor.xml etc. It also internally calls[this
 * call happens everytime the machine agent calls
 * {@code ExtensionMonitor.execute()}] {@code AMonitorJob.run()} -> which in
 * turn calls
 * {@code doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider)}
 * method in this class. {@code AMonitorJob} represents a single run of the
 * extension.
 *
 * {@code ExtensionMonitorTask} (named as "task") in an extension run(named as
 * "Job"). Once all the tasks finish execution, the
 * TaskExecutionServiceProvider(it is the execution service provider for all the
 * tasks in a job), will start DerivedMetricCalculation, print logs related to
 * total metrics sent to the controller in the current job.
 */
public class SnmpTrapReceiver extends ABaseMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiver.class);

	/**
	 * Returns the default metric prefix defined in {@code Constants} to be used if
	 * metric prefix is missing in config.yml Required for
	 * MonitorContextConfiguration initialization
	 *
	 * @return {@code String} the default metrics prefix.
	 */
	@Override
	protected String getDefaultMetricPrefix() {
		return DEFAULT_METRIC_PREFIX;
	}

	/**
	 * Returns the monitor name defined in {@code Constants} Required for
	 * MonitorConfiguration initialization
	 *
	 * @return {@code String} monitor's name
	 */
	@Override
	public String getMonitorName() {
		return MONITOR_NAME;
	}

	/**
	 * The entry point for this class. NOTE: The {@code MetricWriteHelper} is
	 * initialized internally in {@code AMonitorJob}, but it is exposed through
	 * {@code getMetricWriteHelper()} method in {@code TaskExecutionServiceProvider}
	 * class.
	 *
	 * @param tasksExecutionServiceProvider {@code TaskExecutionServiceProvider} is
	 *                                      responsible for finishing all the tasks
	 *                                      before initializing
	 *                                      DerivedMetricsCalculator (It is
	 *                                      basically like a service that executes
	 *                                      the tasks and wait on all of them to
	 *                                      finish and does the finish up work).
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
		
		logger.debug("***** Calling doRun method *****");
		
		
		// reading the server value from the config.yml file
		List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
		AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialized");
		/*
		 * Each task is executed in thread pool, you can have one task to fetch metrics
		 * from each artifact concurrently
		 */
		for (Map<String, String> server : servers) {
			logger.info("***** STARTING TASK FOR SERVER " + server.get("name") + " *****");
			
			logger.debug("***** Creating task object *****");
			SnmpTrapReceiverTask task = new SnmpTrapReceiverTask(getContextConfiguration(),
					tasksExecutionServiceProvider.getMetricWriteHelper(), server);
			
			logger.debug("***** Submitting task object *****");
			tasksExecutionServiceProvider.submit(server.get("name"), task);
			logger.debug("***** task submitted *****");
		}

		CountDownLatch infiniteWait = new CountDownLatch(1);
		try {
			logger.debug("***** Trying infinitewait *****");
			infiniteWait.await();
		} catch (InterruptedException e) {
			logger.error("Failed to wait indefinitely ", e);
		}

	}

	/**
	 * Required by the {@code TaskExecutionServiceProvider} above to know the total
	 * number of tasks it needs to wait on. Think of it as a count in the
	 * {@code CountDownLatch}
	 *
	 * @return Number of tasks, i.e. total number of servers to collect metrics from
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, ?>> getServers() {
		
		//logger.debug("##### getServers method called #####");
		
		List<Map<String, ?>> servers = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
		AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
		return servers;
	}

	@Override
	public void onComplete() {
		logger.info("***** SNMP Trap Receiver Tasks Complete! *****");
	}
	
	/**
	 * This method can be used to initialize any additional arguments (except
	 * config-file which is handled in {@code ABaseMonitor}) configured in
	 * monitor.xml
	 *
	 * @param args A {@code Map<String, String>} of task-arguments configured in
	 *             monitor.xml
	 */
	@Override
	protected void initializeMoreStuff(Map<String, String> args) {
		// Code to initialize additional arguments
	}

}