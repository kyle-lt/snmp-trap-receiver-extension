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
import com.appdynamics.extensions.snmptrapreceiverextension.events.SnmpTrapReceiverEventsManager;
import com.appdynamics.extensions.util.AssertUtils;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.*;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// [kjt] SNMP Trap Receiver Imports
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

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
public class SnmpTrapReceiver extends ABaseMonitor implements CommandResponder {

	private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiver.class);

	// [kjt] SNMP Receiver Member Variables
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;
	private int n = 0;
	private long start = -1;

	// [kjt] Testing Variable Sets
	private String test_listenAddress = "udp:127.0.0.1/16200";
	private String test_username = "username";
	private String test_authpassphrase = "authpassphrase";
	private String test_privacypassphrase = "privacypassphrase";

	// [kjt] SNMP Receiver Methods

	public SnmpTrapReceiver() {
	}

	/*
	 * Moved to doRun() public static void main(String[] args) { new
	 * SnmpTrapReceiver().run(); }
	 */

	private void run() {
		try {
			init();
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void init() throws UnknownHostException, IOException {
		threadPool = ThreadPool.create("Trap", 10);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		// TRANSPORT
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", test_listenAddress));
		
		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
		} else {
			transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
		}

		// V3 SECURITY
		USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(),
				new OctetString(MPv3.createLocalEngineID()), 0);

		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES192());
		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256());
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		usm.setEngineDiscoveryEnabled(true);

		SecurityModels.getInstance().addSecurityModel(usm);

		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));

		String username = test_username; // SET THIS
		String authpassphrase = test_authpassphrase; // SET THIS
		String privacypassphrase = test_privacypassphrase; // SET THIS

		snmp.getUSM().addUser( // SET THE SECURITY PROTOCOLS HERE
				new OctetString(username), new UsmUser(new OctetString(username), AuthMD5.ID,
						new OctetString(authpassphrase), PrivAES128.ID, new OctetString(privacypassphrase)));

		snmp.listen();
	}

	public void processPdu(CommandResponderEvent crEvent) {
		PDU pdu = crEvent.getPDU();
		if (pdu.getType() == PDU.V1TRAP) {

			PDUv1 pduV1 = (PDUv1) pdu;
			System.out.println("");
			logger.info("");
			System.out.println("===== NEW SNMP 1 TRAP RECEIVED ====");
			logger.info("===== NEW SNMP 1 TRAP RECEIVED ====");
			System.out.println("agentAddr " + pduV1.getAgentAddress().toString());
			logger.info("agentAddr " + pduV1.getAgentAddress().toString());
			System.out.println("enterprise " + pduV1.getEnterprise().toString());
			logger.info("enterprise " + pduV1.getEnterprise().toString());
			System.out.println("timeStam" + String.valueOf(pduV1.getTimestamp()));
			logger.info("timeStamp" + String.valueOf(pduV1.getTimestamp()));
			System.out.println("genericTrap" + String.valueOf(pduV1.getGenericTrap()));
			logger.info("genericTrap" + String.valueOf(pduV1.getGenericTrap()));
			System.out.println("specificTrap " + String.valueOf(pduV1.getSpecificTrap()));
			logger.info("specificTrap " + String.valueOf(pduV1.getSpecificTrap()));
			System.out.println("snmpVersion " + String.valueOf(PDU.V1TRAP));
			logger.info("snmpVersion " + String.valueOf(PDU.V1TRAP));
			System.out.println("communityString " + new String(crEvent.getSecurityName()));
			logger.info("communityString " + new String(crEvent.getSecurityName()));

		} else if (pdu.getType() == PDU.TRAP) {
			System.out.println("");
			logger.info("");
			System.out.println("===== NEW SNMP 2/3 TRAP RECEIVED ====");
			logger.info("===== NEW SNMP 2/3 TRAP RECEIVED ====");

			
			System.out.println("errorStatus " + String.valueOf(pdu.getErrorStatus()));
			logger.info("errorStatus " + String.valueOf(pdu.getErrorStatus()));
			System.out.println("errorIndex " + String.valueOf(pdu.getErrorIndex()));
			logger.info("errorIndex " + String.valueOf(pdu.getErrorIndex()));
			System.out.println("requestID " + String.valueOf(pdu.getRequestID()));
			logger.info("requestID " + String.valueOf(pdu.getRequestID()));
			System.out.println("snmpVersion " + String.valueOf(PDU.TRAP));
			logger.info("snmpVersion " + String.valueOf(PDU.TRAP));
			System.out.println("communityString " + new String(crEvent.getSecurityName()));
			logger.info("communityString " + new String(crEvent.getSecurityName()));

		}

		Vector<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		if (varBinds != null && !varBinds.isEmpty()) {
			Iterator<? extends VariableBinding> varIter = varBinds.iterator();

			StringBuilder resultset = new StringBuilder();
			resultset.append("-----");
			while (varIter.hasNext()) {
				VariableBinding vb = varIter.next();

				String syntaxstr = vb.getVariable().getSyntaxString();
				int syntax = vb.getVariable().getSyntax();
				System.out.println("OID: " + vb.getOid());
				logger.info("OID: " + vb.getOid());
				System.out.println("Value: " + vb.getVariable());
				logger.info("Value: " + vb.getVariable());
				System.out.println("syntaxstring: " + syntaxstr);
				logger.info("syntaxstring: " + syntaxstr);
				System.out.println("syntax: " + syntax);
				logger.info("syntax: " + syntax);
				System.out.println("------");
				logger.info("------");
			}

		}
		System.out.println("==== TRAP END ===");
		logger.info("==== TRAP END ===");
		System.out.println("");
		logger.info("");
	}

	/**
	 * Returns the default metric prefix defined in {@code Constants} to be used if
	 * metric prefix is missing in config.yml Required for
	 * MonitorContextConfiguration initialisation
	 *
	 * @return {@code String} the default metrics prefix.
	 */
	@Override
	protected String getDefaultMetricPrefix() {
		return DEFAULT_METRIC_PREFIX;
	}

	/**
	 * Returns the monitor name defined in {@code Constants} Required for
	 * MonitorConfiguration initialisation
	 *
	 * @return {@code String} monitor's name
	 */
	@Override
	public String getMonitorName() {
		return MONITOR_NAME;
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

	/**
	 * The entry point for this class. NOTE: The {@code MetricWriteHelper} is
	 * initialised internally in {@code AMonitorJob}, but it is exposed through
	 * {@code getMetricWriteHelper()} method in {@code TaskExecutionServiceProvider}
	 * class.
	 *
	 * @param tasksExecutionServiceProvider {@code TaskExecutionServiceProvider} is
	 *                                      responsible for finishing all the tasks
	 *                                      before initialising
	 *                                      DerivedMetricsCalculator (It is
	 *                                      basically like a service that executes
	 *                                      the tasks and wait on all of them to
	 *                                      finish and does the finish up work).
	 */
	@Override
	protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
		// reading a value from the config.yml file
		// List<Map<String,String>> servers =
		// (List<Map<String,String>>)getContextConfiguration().getConfigYml().get("servers");
		// AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is
		// not initialised");
		/*
		 * Each task is executed in thread pool, you can have one task to fetch metrics
		 * from each artifact concurrently
		 */
		// for (Map<String, String> server : servers) {
		// SnmpTrapReceiverTask task = new
		// SnmpTrapReceiverTask(getContextConfiguration(),
		// tasksExecutionServiceProvider.getMetricWriteHelper(), server);
		// tasksExecutionServiceProvider.submit(server.get("name"),task);
		// }

		new SnmpTrapReceiver().run();

	}

	/**
	 * Required by the {@code TaskExecutionServiceProvider} above to know the total
	 * number of tasks it needs to wait on. Think of it as a count in the
	 * {@code CountDownLatch}
	 *
	 * @return Number of tasks, i.e. total number of servers to collect metrics from
	 */
	@Override
	protected List<Map<String, ?>> getServers() {
		List<Map<String, ?>> servers = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
		AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
		return servers;
	}
	

	/*
	//@Override
	public void onComplete() {
		SnmpTrapReceiverEventsManager extensionStarterEventsManager = new SnmpTrapReceiverEventsManager(
				getContextConfiguration().getContext().getEventsServiceDataManager());
		try {
			extensionStarterEventsManager.createSchema();
			extensionStarterEventsManager.updateSchema();
			extensionStarterEventsManager.publishEvents();
		} catch (Exception ex) {
			logger.error("Error encountered while publishing events");
		}
	}
	*/
}