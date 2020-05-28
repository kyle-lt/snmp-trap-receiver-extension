/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.snmptrapreceiverextension;

/**
 * Created by bhuvnesh.kumar on 12/15/17.
 */

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.DEFAULT_METRIC_SEPARATOR;
import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.METRICS;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//[kjt] SNMP Trap Receiver Imports
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

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
 * The ExtensionMonitorTask(namely "task") is an instance of {@link Runnable}
 * needs to implement the interface {@code AMonitorTaskRunnable} instead of
 * {@code Runnable}. This would make the need for overriding
 * {@code onTaskComplete()} method which will be called once the {@code run()}
 * method execution is done.
 *
 */
public class SnmpTrapReceiverTask implements AMonitorTaskRunnable, CommandResponder {

	private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiverTask.class);
	private MonitorContextConfiguration configuration;
	private MetricWriteHelper metricWriteHelper;
	private Map<String, String> server;
	private String metricPrefix;
	private List<Map<String, ?>> metricList;

	// [kjt] SNMP Receiver Member Variables
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;
	//private int n = 0;
	//private long start = -1;

	// [kjt] Testing Variable Sets
	private String test_listenAddress = "udp:0.0.0.0/16200";
	private String test_username = "username";
	private String test_authpassphrase = "authpassphrase";
	private String test_privacypassphrase = "privacypassphrase";

	// [kjt] SNMP Receiver Methods

	private void snmpRun() {
		
		logger.debug("##### snmpRun method called #####");
		
		try {
			init();
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			logger.error("Exception occured during snmpRun", ex);
			ex.printStackTrace();
		}
	}

	private void init() throws UnknownHostException, IOException {

		logger.debug("##### init method called #####");

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

		logger.debug("##### processPdu method called #####");

		PDU pdu = crEvent.getPDU();
		if (pdu.getType() == PDU.V1TRAP) {

			PDUv1 pduV1 = (PDUv1) pdu;
			logger.debug("===== NEW SNMP 1 TRAP RECEIVED ====");
			logger.debug("agentAddr " + pduV1.getAgentAddress().toString());
			logger.debug("enterprise " + pduV1.getEnterprise().toString());
			logger.debug("timeStamp" + String.valueOf(pduV1.getTimestamp()));
			logger.debug("genericTrap" + String.valueOf(pduV1.getGenericTrap()));
			logger.debug("specificTrap " + String.valueOf(pduV1.getSpecificTrap()));
			logger.debug("snmpVersion " + String.valueOf(PDU.V1TRAP));
			logger.debug("communityString " + new String(crEvent.getSecurityName()));

		} else if (pdu.getType() == PDU.TRAP) {

			logger.debug("===== NEW SNMP 2/3 TRAP RECEIVED ====");
			logger.debug("errorStatus " + String.valueOf(pdu.getErrorStatus()));
			logger.debug("errorIndex " + String.valueOf(pdu.getErrorIndex()));
			logger.debug("requestID " + String.valueOf(pdu.getRequestID()));
			logger.debug("snmpVersion " + String.valueOf(PDU.TRAP));
			logger.debug("communityString " + new String(crEvent.getSecurityName()));

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
				logger.debug("OID: " + vb.getOid());
				logger.debug("Value: " + vb.getVariable());
				logger.debug("syntaxstring: " + syntaxstr);
				logger.debug("syntax: " + syntax);
			}

		}
		logger.debug("==== TRAP END ===");		
	}

	public SnmpTrapReceiverTask(MonitorContextConfiguration configuration, MetricWriteHelper metricWriteHelper,
			Map<String, String> server) {

		logger.debug("##### SnmpTrapReceiverTask method called #####");

		this.configuration = configuration;
		this.metricWriteHelper = metricWriteHelper;
		this.server = server;
		this.metricPrefix = configuration.getMetricPrefix();
		this.metricList = (List<Map<String, ?>>) configuration.getConfigYml().get(METRICS);

		logger.debug("##### test_listenAddress1 = " + test_listenAddress + " #####");
		logger.debug("##### test_username1 = " + test_username + " #####");
		logger.debug("##### test_authpassphrase1 = " + test_authpassphrase + " #####");
		logger.debug("##### test_privacypassphrase1 = " + test_privacypassphrase + " #####");

		AssertUtils.assertNotNull(this.metricList, "The 'metrics' section in config.yml is either null or empty");
	}

	/**
	 * This method contains the main business logic of the extension.
	 */
	@Override
	public void run() {

		logger.debug("##### Debug messages look like this #####");
        logger.info("***** Info messages look like this *****");

        logger.debug("##### run method called #####");
		logger.info("***** Creating new SnmpTrapReceiverTask to listen for SNMP Traps *****");
		logger.info("***** Created task and started working for Server: {}", server.get("name") + " *****");

		this.snmpRun();

		/*
		 * It is in this function that you can get your metrics and process them and
		 * send them to the controller. You can look at the various extensions available
		 * on the community site and build your extension based on them.
		 *
		 */

		/*
		 * Once you have collected the required metrics you can send them to the metric
		 * browser as shown in the below example. In this example, let's assume that you
		 * have pulled a metric called CPU Utilization, refer config.yml to configure
		 * what metrics you need to collect, you will create a metric object and add it
		 * to a list. The list hold all the metric object and using the method shown in
		 * example you can send all the metrics to the metric browser. NOTE: the
		 * underlying piece of code is designed to handle the specific way in which the
		 * 'metrics' section of config.yml is structured, please modify it according to
		 * your structure definition in config.yml
		 */
		// get list of metrics to pull from 'metrics' section in config.yml
		// iterate through all the metrics and add them to a list
		List<Metric> metrics = new ArrayList<>();
		for (Map<String, ?> metricType : metricList) {
			for (Map.Entry<String, ?> entry : metricType.entrySet()) {
				// get details of the specific metric, in this example 'CPUUtilization' metric
				// in config.yml
				String metricName = entry.getKey();
				logger.info("Building metric for {}", metricName);
				Map<String, ?> metricProperties = (Map<String, ?>) entry.getValue();
				buildMetric(metrics, metricName, metricProperties);
			}
		}
		// generateMetricsForCharReplacement(metrics);
		metricWriteHelper.transformAndPrintMetrics(metrics);
	}

	/**
	 * Creates a {@code Metric} object and add it to the {@code List<Metrics>}
	 * 
	 * @param metrics          A {@code List<Metric>} updated by the method
	 * @param metricProperties Properties of the metric type
	 */
	private void buildMetric(List<Metric> metrics, String metricName, Map<String, ?> metricProperties) {

		logger.debug("##### buildMetric method called #####");

		// this example uses a hardcoded value (20),
		// use the value that you get for your metrics, you can modify the method
		// signature to
		// pass the actual value of the metric
		// You can look at the various extensions available on the community site for
		// further understanding
		Metric metric = new Metric(metricName, String.valueOf(20), metricPrefix + DEFAULT_METRIC_SEPARATOR + metricName,
				metricProperties);
		metrics.add(metric);
	}

	/**
	 * This onTaskComplete() method emphasizes the need to print metrics like
	 * "METRICS_COLLECTION_STATUS" or do any other task complete work.
	 */
	@Override
	public void onTaskComplete() {

		logger.debug("##### onTaskComplete method called #####");

		/*
		 * Below code shows an example of how to print metrics
		 */
		List<Metric> metrics = new ArrayList<>();

		// This creates the Heart Beat Metric with default properties
		Metric metric = new Metric("Heart Beat", String.valueOf(BigInteger.ONE),
				metricPrefix + DEFAULT_METRIC_SEPARATOR + " Heart Beat");
		metrics.add(metric);
		metricWriteHelper.transformAndPrintMetrics(metrics);
		logger.info("Completed task for Server: {}", server.get("name"));
	}
	/*
	 * private void generateMetricsForCharReplacement(List<Metric> metrics) { Metric
	 * metric1 = new Metric("Pipe|", "10", new HashMap<String, Object>(),
	 * "Custom Metrics|Extension Starter CI|Character Replacement|","Pipe|" );
	 * Metric metric2 = new Metric("Comma,", "10", new HashMap<String, Object>(),
	 * "Custom Metrics|Extension Starter CI|Character Replacement|","Comma," );
	 * Metric metric3 = new Metric(":Colon", "10", new HashMap<String, Object>(),
	 * "Custom Metrics|Extension Starter CI|Character Replacement|",":Colon" );
	 * Metric metric4 = new Metric("Memóry Free", "10", new HashMap<String,
	 * Object>(),
	 * "Custom Metrics|Extension Starter CI|Character Replacement|","Memóry Free" );
	 * Metric metric5 = new Metric("Memory Üsed", "10", new HashMap<String,
	 * Object>(), "Custom Metrics|Extension Starter CI|Character Replacement|"
	 * ,"Memory \u00dcsed" ); Metric metric6 = new Metric("Question?Mark", "10", new
	 * HashMap<String, Object>(),
	 * "Custom Metrics|Extension Starter CI|Character Replacement|","Question?Mark"
	 * ); metrics.add(metric1); metrics.add(metric2); metrics.add(metric3);
	 * metrics.add(metric4); metrics.add(metric5); metrics.add(metric6); }
	 */

}