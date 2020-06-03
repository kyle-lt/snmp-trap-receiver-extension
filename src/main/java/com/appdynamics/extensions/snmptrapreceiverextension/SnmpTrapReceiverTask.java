/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.snmptrapreceiverextension;

/**
 * Created by Kyle Tully on 05/26/2020
 */

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.DEFAULT_METRIC_SEPARATOR;
import static com.appdynamics.extensions.snmptrapreceiverextension.util.Constants.METRICS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

//[kjt] SNMP Trap Receiver Imports
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

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
//import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
//import org.snmp4j.security.PrivDES;
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
import org.yaml.snakeyaml.emitter.EmitterException;

/**
 * The SnmpTrapReceiverTask is an instance of {@link Runnable} and needs to
 * implement the interface {@link AMonitorTaskRunnable} instead of
 * {@code Runnable}. This requires overriding the {@code onTaskComplete()}
 * method which will be called once the {@code run()} method execution is done.
 * 
 * This particular class also implements {@link CommandResponder} in order to
 * receive SNMP Traps.
 *
 */
public class SnmpTrapReceiverTask implements AMonitorTaskRunnable, CommandResponder {

	private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiverTask.class);
	private MonitorContextConfiguration configuration;
	private MetricWriteHelper metricWriteHelper;
	private Map<String, String> server;
	private String metricPrefix;
	private List<Map<String, ?>> metricList;

	// [kjt] Extension Connection Variables
	private String machineAgentHost;
	private String machineAgentPort;

	private String snmpListenAddress;
	private String snmpUsername;
	private String snmpAuthPassPhrase;
	private String snmpPrivacyPassPhrase;

	private static final String EVENT_PUBLISHING_URL = "http://localhost:8293/api/v1/events";
	// private static final String MA_HTTP_URI = "/api/v1/events";

	// [kjt] SNMP Receiver Member Variables
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;
	private int incomingTraps;
	private int outgoingEvents;

	// [kjt] Testing Variable Sets
	private String test_listenAddress = "udp:0.0.0.0/16200";
	private String test_username = "username";
	private String test_authpassphrase = "authpassphrase";
	private String test_privacypassphrase = "privacypassphrase";

	// [kjt] SNMP Receiver Methods

	private void snmpRun() {

		logger.debug("***** Calling snmpRun *****");
		try {
			init();
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			logger.error("Exception occured during snmpRun", ex);
			ex.printStackTrace();
		}
	}

	private void init() throws UnknownHostException, IOException {

		logger.debug("***** Calling init *****");

		threadPool = ThreadPool.create("Trap", 10);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		// TRANSPORT
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", this.snmpListenAddress));

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

		String username = this.snmpUsername;
		String authpassphrase = this.snmpAuthPassPhrase;
		String privacypassphrase = this.snmpPrivacyPassPhrase;

		// What else do I need to configure in order to cover all authentication
		// methods? TODO

		snmp.getUSM().addUser(new OctetString(username), new UsmUser(new OctetString(username), AuthMD5.ID,
				new OctetString(authpassphrase), PrivAES128.ID, new OctetString(privacypassphrase)));

		snmp.listen();
	}

	/**
	 * This method processes the protocol data unit (PDU) which contains the trap
	 * information that will be sent to AppD as a Custom Event.
	 */
	public synchronized void processPdu(CommandResponderEvent e) {

		PDU pdu = e.getPDU();
		logger.debug("pdu.toString(): " + pdu.toString());

		// Increment Trap counter
		incomingTraps++;

		// Extract Variable Bindings
		List<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		if (varBinds != null && !varBinds.isEmpty()) {

			Iterator<? extends VariableBinding> varIter = varBinds.iterator();
			JsonArray jsonArr = null;
			JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			JsonObjectBuilder jsonDetailsObjectBuilder = Json.createObjectBuilder();

			jsonObjectBuilder.add("eventSeverity", "INFO").add("type", "SNMP Trap").add("summaryMessage", "SNMP Trap")
					.add("properties", Json.createObjectBuilder());

			if (pdu.getType() == PDU.V1TRAP) {

				PDUv1 pduV1 = (PDUv1) pdu;

				jsonDetailsObjectBuilder.add("Type", String.valueOf(pduV1.getType()))
						.add("AgentAddress", pduV1.getAgentAddress().toString())
						.add("Enterprise", pduV1.getEnterprise().toString())
						.add("TimeStamp", String.valueOf(pduV1.getTimestamp()))
						.add("GenericTrap", String.valueOf(pduV1.getGenericTrap()))
						.add("SpecificTrap", String.valueOf(pduV1.getSpecificTrap()))
						.add("SnmpVersion", String.valueOf(PDU.V1TRAP)).add("SnmpVersion", String.valueOf(PDU.V1TRAP))
						.add("CommunityString", new String(e.getSecurityName()));

				logger.debug("SNMP v1 TRAP RECEIVED");
				logger.debug("Type " + String.valueOf(pduV1.getType()));
				logger.debug("AgentAddress " + pduV1.getAgentAddress().toString());
				logger.debug("Enterprise " + pduV1.getEnterprise().toString());
				logger.debug("TimeStamp" + String.valueOf(pduV1.getTimestamp()));
				logger.debug("GenericTrap" + String.valueOf(pduV1.getGenericTrap()));
				logger.debug("SpecificTrap " + String.valueOf(pduV1.getSpecificTrap()));
				logger.debug("SnmpVersion " + String.valueOf(PDU.V1TRAP));
				logger.debug("CommunityString " + new String(e.getSecurityName()));

			} else if (pdu.getType() == PDU.TRAP) {

				jsonDetailsObjectBuilder.add("ErrorStatus", String.valueOf(pdu.getErrorStatus()))
						.add("ErrorStatusText", String.valueOf(pdu.getErrorStatusText()))
						.add("ErrorIndex", String.valueOf(pdu.getErrorIndex()))
						.add("RequestID", String.valueOf(pdu.getRequestID()))
						.add("MaxRepetitions", String.valueOf(pdu.getMaxRepetitions()))
						.add("NonRepeaters", String.valueOf(pdu.getNonRepeaters()))
						.add("SnmpVersion", String.valueOf(PDU.V1TRAP))
						.add("CommunityString", new String(e.getSecurityName()));

				logger.debug("SNMP v2/v3 TRAP RECEIVED");
				logger.debug("ErrorStatus " + String.valueOf(pdu.getErrorStatus()));
				logger.debug("ErrorStatusText " + String.valueOf(pdu.getErrorStatusText()));
				logger.debug("ErrorIndex " + String.valueOf(pdu.getErrorIndex()));
				logger.debug("RequestID " + String.valueOf(pdu.getRequestID()));
				logger.debug("MaxRepetitions " + String.valueOf(pdu.getMaxRepetitions()));
				logger.debug("NonRepeaters " + String.valueOf(pdu.getNonRepeaters()));
				logger.debug("SnmpVersion " + String.valueOf(PDU.TRAP));
				logger.debug("CommunityString " + new String(e.getSecurityName()));

			}

			int counter = 0;
			while (varIter.hasNext()) {
				VariableBinding vb = varIter.next();

				jsonDetailsObjectBuilder.add(Integer.toString(counter) + "_OID", vb.getOid().toString())
						.add(Integer.toString(counter) + "_Value", vb.getVariable().toString())
						.add(Integer.toString(counter) + "_SyntaxString_", vb.getVariable().getSyntaxString())
						.add(Integer.toString(counter) + "_Syntax_", Integer.toString(vb.getVariable().getSyntax()));

				logger.debug(Integer.toString(counter) + "_OID: " + vb.getOid());
				logger.debug(Integer.toString(counter) + "_Value: " + vb.getVariable());
				logger.debug(Integer.toString(counter) + "_SyntaxString: " + vb.getVariable().getSyntaxString());
				logger.debug(Integer.toString(counter) + "_Syntax: " + Integer.toString(vb.getVariable().getSyntax()));
				counter++;

			}

			jsonObjectBuilder.add("details", jsonDetailsObjectBuilder);

			jsonArr = jsonArrayBuilder.add(jsonObjectBuilder).build();

			sendEvent(jsonArr);
		}
		logger.debug("TRAP END ");
	}

	private void sendEvent(JsonArray json) {

		logger.debug("JSON Body String: " + json.toString());

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(EVENT_PUBLISHING_URL);
		StringEntity stringEntity = null;

		try {
			stringEntity = new StringEntity(json.toString());
		} catch (Exception e) {
			logger.error("Error while creating StringEntity", e);
		}

		httpPost.setEntity(stringEntity);
		httpPost.setHeader("Content-Type", "application/json");
		// httpPost.setHeader("Accept", "application/json");

		try {
			HttpResponse response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300) {
				outgoingEvents++;
				logger.info("Event published to controller");
			} else {
				logger.error("Unexpected response : " + response);
			}
		} catch (IOException e) {
			logger.error("Error while posting event to controller", e);
		}
	}

	@SuppressWarnings("unchecked")
	public SnmpTrapReceiverTask(MonitorContextConfiguration configuration, MetricWriteHelper metricWriteHelper,
			Map<String, String> server) {

		logger.debug("***** Calling SnmpTrapReceiverTask *****");

		this.configuration = configuration;
		this.metricWriteHelper = metricWriteHelper;
		this.server = server;
		this.metricPrefix = configuration.getMetricPrefix();
		this.metricList = (List<Map<String, ?>>) configuration.getConfigYml().get(METRICS);
		Map<String, ?> configYaml = (Map<String, ?>) configuration.getConfigYml();

		// Retrieve configurations for the Machine Agent HTTP Listener Host and Port
		try {
			//logger.debug("### machineAgentConnection YAML: " + configYaml.get("machineAgentConnection"));
			Map<String, ?> machineAgentConnMaps = (Map<String, ?>) configYaml.get("machineAgentConnection");
			logger.debug("### MachineAgentConnMaps YAML: " + machineAgentConnMaps);
			String maHostYaml = (String) machineAgentConnMaps.get("host");
			//String maPortYaml = (String) machineAgentConnMaps.get("port");
			//this.machineAgentHost = (String) machineAgentConnMaps.get("host");
			//this.machineAgentPort = (String) machineAgentConnMaps.get("port");
			logger.debug("### MachineAgent Host YAML: " + maHostYaml);
			//logger.debug("### MachineAgent Port YAML: " + maPortYaml);
			logger.debug("### Machine Agent Host: " + (String) machineAgentConnMaps.get("host"));
			logger.debug("### Machine Agent Port: " + (String) machineAgentConnMaps.get("port"));
		} catch (EmitterException e) {
			logger.error("Failed to capture Machine Agent Connection Config from yaml", e);
		}
		
		

		// this.machineAgentHost = (String) machineAgentConn.get("host");
		// this.machineAgentPort = (String) machineAgentConn.get("port");

		// Retrieve configurations for the SNMP Listener Address and Security settings
		// Map<String, ?> snmpConn = (Map<String, ?>)
		// configuration.getConfigYml().get("snmpConnection");

		this.snmpListenAddress = test_listenAddress;
		this.snmpUsername = test_username;
		this.snmpAuthPassPhrase = test_authpassphrase;
		this.snmpPrivacyPassPhrase = test_privacypassphrase;

		// this.snmpListenAddress = (String) snmpConn.get("snmpProtocol") + ":" +
		// (String) snmpConn.get("snmpIP") + "/" +
		// (String) snmpConn.get("snmpPort");

		// this.snmpUsername = (String) snmpConn.get("snmpUsername");
		// this.snmpAuthPassPhrase = (String) snmpConn.get("snmpAuthPassPhrase");
		// this.snmpPrivacyPassPhrase = (String) snmpConn.get("privacypassphrase");

		logger.debug("Machine Agent Host = " + this.machineAgentHost);
		logger.debug("Machine Agent Port = " + this.machineAgentPort);
		logger.debug("SNMP Listen Address = " + this.snmpListenAddress);
		logger.debug("SNMP Username = " + this.snmpUsername);
		logger.debug("SNMP Auth Pass Phrase = " + this.snmpAuthPassPhrase);
		logger.debug("SNMP Privacy Pass Phrase = " + this.snmpPrivacyPassPhrase);

		AssertUtils.assertNotNull(this.metricList, "The 'metrics' section in config.yml is either null or empty");
	}

	/**
	 * This method contains the main business logic of the extension.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		logger.info("Creating new SnmpTrapReceiverTask to listen for SNMP Traps");
		logger.debug("***** Calling Run *****");
		// logger.info("Created task and started working for Server: {}",
		// server.get("name"));

		this.snmpRun();

		// get list of metrics to pull from 'metrics' section in config.yml
		// iterate through all the metrics and add them to a list

		// This Task is running continuously, so we'll manage sending the metrics
		// every minute here manually. We iterate through the metrics
		incomingTraps = 0;
		outgoingEvents = 0;

		while (true) {
			// Iterate through metrics defined in config.yml and build them
			List<Metric> metrics = new ArrayList<>();
			for (Map<String, ?> metricType : metricList) {
				for (Map.Entry<String, ?> entry : metricType.entrySet()) {
					String metricName = entry.getKey();
					logger.info("Building metric for {}", metricName);
					Map<String, ?> metricProperties = (Map<String, ?>) entry.getValue();
					buildMetric(metrics, metricName, metricProperties);
				}
			}

			// Print metrics to log
			metricWriteHelper.transformAndPrintMetrics(metrics);

			// Reset counters
			incomingTraps = 0;
			outgoingEvents = 0;

			// Now that metrics are done, wait 60000ms (60s)
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				logger.error("Sleep failed", e);
			}
		}
	}

	/**
	 * Creates a {@code Metric} object and add it to the {@code List<Metrics>}
	 * 
	 * @param metrics          A {@code List<Metric>} updated by the method
	 * @param metricProperties Properties of the metric type
	 */
	private void buildMetric(List<Metric> metrics, String metricName, Map<String, ?> metricProperties) {

		Metric metric;

		switch (metricName) {
		case "IncomingTraps":
			metric = new Metric(metricName, String.valueOf(incomingTraps),
					metricPrefix + DEFAULT_METRIC_SEPARATOR + metricName, metricProperties);
			break;
		case "OutgoingEvents":
			metric = new Metric(metricName, String.valueOf(outgoingEvents),
					metricPrefix + DEFAULT_METRIC_SEPARATOR + metricName, metricProperties);
			break;
		// Heart Beat
		default:
			metric = new Metric(metricName, String.valueOf(1), metricPrefix + DEFAULT_METRIC_SEPARATOR + metricName,
					metricProperties);
		}

		metrics.add(metric);
	}

	/**
	 * This onTaskComplete() method emphasizes the need to print metrics like
	 * "METRICS_COLLECTION_STATUS" or do any other task complete work.
	 */
	@Override
	public void onTaskComplete() {

		logger.info("Completed task for Server: {}", server.get("name"));
	}

}