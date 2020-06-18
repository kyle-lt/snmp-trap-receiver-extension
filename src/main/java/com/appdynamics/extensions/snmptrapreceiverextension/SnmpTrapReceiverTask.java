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
//import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;

import com.appdynamics.extensions.snmptrapreceiverextension.events.SnmpTrapReceiverEventsManager;

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
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

//[kjt] SNMP Trap Receiver Imports
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
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
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
//import org.snmp4j.transport.TransportListener;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
//import org.yaml.snakeyaml.emitter.EmitterException;

/**
 * The SnmpTrapReceiverTask is an instance of {@link Runnable} and needs to
 * implement the interface {@link AMonitorTaskRunnable} instead of
 * {@code Runnable}. This requires overriding the {@code onTaskComplete()}
 * method which will be called once the {@code run()} method execution is done.
 * 
 * This particular class also implements {@link CommandResponder} in order to
 * receive SNMP Traps.
 * 
 * @author kjt
 *
 */
public class SnmpTrapReceiverTask implements AMonitorTaskRunnable, CommandResponder {

	private static final Logger logger = LoggerFactory.getLogger(SnmpTrapReceiverTask.class);
	private MonitorContextConfiguration configuration;
	private MetricWriteHelper metricWriteHelper;
	private Map<String, String> server;
	private String metricPrefix;
	private List<Map<String, ?>> metricList;

	// Extension Connection Variables - read from config.yml
	private String machineAgentHost;
	private String machineAgentPort;
	private String snmpListenAddress;
	private String snmpUsername;
	private String snmpAuthProtocol;
	private String snmpAuthPassPhrase;
	private String snmpPrivacyProtocol;
	private String snmpPrivacyPassPhrase;

	private static final String MA_HTTP_URI = "/api/v1/events";

	// SNMP Receiver Member Variables
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;
	private int incomingTraps;
	private int outgoingEvents;

	/**
	 * The snmpRun function makes the call to initialize the SNMP Listener and then
	 * adds the {@link CommandResponder} in order to process PDUs received by the
	 * listener.
	 */
	private void snmpRun() {

		try {
			//DEBUGGING
			SNMP4JSettings.setForwardRuntimeExceptions(true);
			
			init();
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			logger.error("Exception occured during snmpRun", ex);
			ex.printStackTrace();
		}
	}

	/**
	 * The {@code init} function initializes the SNMP Listener configurations for listen 
	 * address (protocol, IP, port) and security settings ( community string for
	 * v1 and v2c, and user, authpassphrase, and privacypassphrase for v3).
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void init() throws UnknownHostException, IOException {
		
		threadPool = ThreadPool.create("Trap", 10);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", this.snmpListenAddress));

		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
		} else {
			transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
		}

		// Initialize and configure v3 Security
		USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		
		usm.setEngineDiscoveryEnabled(true);

		// Enable the ability to utilize all privacy/encryption protocols
		// Specific choice of desired protocol is user-provided in config.yml
		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());
		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES192());
		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256());
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());
		SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());

		SecurityModels.getInstance().addSecurityModel(usm);

		snmp = new Snmp(dispatcher, transport);
		
		// Enable the ability to receive PDUs from all 3 SNMP versions
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));

		// Set some local variables to use for initializing and configuring the v3 security
		// All of these values should have been configured in the config.yml if using v3
		String username = this.snmpUsername;
		String authpassphrase = this.snmpAuthPassPhrase;
		String privacypassphrase = this.snmpPrivacyPassPhrase;
		OID authProtocol = null;
		OID privacyProtocol = null;

		// Determine and set authentication protocol
		if (this.snmpAuthProtocol.equals("MD5")) {
			authProtocol = AuthMD5.ID;
		} else if (this.snmpAuthProtocol.equals("SHA"))  {
			authProtocol = AuthSHA.ID;
		} else {
			logger.warn("Authentication Protocol provided \"" + this.snmpAuthProtocol + "\" not found. Defaulting to MD5");
			authProtocol = AuthMD5.ID;
		}

		// Determine and set privacy/encryption protocol
		switch (this.snmpPrivacyProtocol) {
		case "AES128":
			privacyProtocol = PrivAES128.ID;
			break;
		case "AES192":
			privacyProtocol = PrivAES192.ID;
			break;
		case "AES256":
			privacyProtocol = PrivAES256.ID;
			break;
		case "DES":
			privacyProtocol = PrivDES.ID;
			break;
		case "3DES":
			privacyProtocol = Priv3DES.ID;
			break;
		default:
			logger.warn("Privacy Protocol provided \"" + this.snmpPrivacyProtocol + "\" not found. Defaulting to AES128");
			privacyProtocol = PrivAES128.ID;
		}

		Boolean isLocalized = false;
		
		// Add User to the USM
		snmp.getUSM().addUser(new OctetString(username),
				new UsmUser(new OctetString(username), authProtocol, new OctetString(authpassphrase),
						privacyProtocol, new OctetString(privacypassphrase)));
		isLocalized = snmp.getUSM().getUser(new OctetString(snmp.getLocalEngineID()), new OctetString(username)).getUsmUser().isLocalized();
		logger.debug("Is this user localized? " + isLocalized.toString());
		
		logger.debug("*** SNMP snmp.getUSM().getLocalEngineID() - OctetString: " + snmp.getUSM().getLocalEngineID());
		logger.debug("*** SNMP.GetUSM.GetUser using snmp.getUSM().getLocalEngineID():           " + snmp.getUSM().getUser(snmp.getUSM().getLocalEngineID(), new OctetString(username)));
		snmp.listen();

	}

	/**
	 * This method processes the protocol data unit (PDU) which contains the trap
	 * information that will be sent to AppD as a Custom Event. It builds a JSON
	 * Array and calls the {@link sendEvent} method to eventually send the even to
	 * the AppD Controller.
	 */
	public synchronized void processPdu(CommandResponderEvent e) {

		PDU pdu = e.getPDU();
		
		logger.debug("pdu.toString(): " + pdu.toString());
		logger.debug("e.toString(): " + e.toString());
		logger.debug("e.getTransportMapping(): " + e.getTransportMapping().toString());

		// Increment Trap counter
		incomingTraps++;

		// Extract Variable Bindings
		List<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		if (varBinds != null && !varBinds.isEmpty()) {

			Iterator<? extends VariableBinding> varIter = varBinds.iterator();
			JsonArray jsonArr = null;
			JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			JsonObjectBuilder jsonPropertiesObjectBuilder = Json.createObjectBuilder();
			JsonObjectBuilder jsonDetailsObjectBuilder = Json.createObjectBuilder();

			// Build the basic properties required for a Custom Event
			jsonObjectBuilder.add("eventSeverity", "INFO").add("type", "SNMP Trap").add("summaryMessage", "SNMP Trap");

			// Build the properties section, which can be used to filter the Custom Events
			jsonPropertiesObjectBuilder.add("Type", "SNMP Trap");
			jsonObjectBuilder.add("properties", jsonPropertiesObjectBuilder);

			// Build the details section, which contains the metadata for the Custom Event
			
			// Build the details for a V1 TRAP PDU
			if (pdu.getType() == PDU.V1TRAP) {

				PDUv1 pduV1 = (PDUv1) pdu;

				jsonDetailsObjectBuilder.add("Type", String.valueOf(pduV1.getType()))
						.add("AgentAddress", pduV1.getAgentAddress().toString())
						.add("Enterprise", pduV1.getEnterprise().toString())
						.add("TimeStamp", String.valueOf(pduV1.getTimestamp()))
						.add("GenericTrap", String.valueOf(pduV1.getGenericTrap()))
						.add("SpecificTrap", String.valueOf(pduV1.getSpecificTrap()))						
						.add("SnmpVersion", "v1")
						.add("CommunityString", new String(e.getSecurityName()));

				logger.debug("SNMP v1 TRAP RECEIVED");
				logger.debug("Type:            " + String.valueOf(pduV1.getType()));
				logger.debug("AgentAddress:    " + pduV1.getAgentAddress().toString());
				logger.debug("Enterprise:      " + pduV1.getEnterprise().toString());
				logger.debug("TimeStamp:       " + String.valueOf(pduV1.getTimestamp()));
				logger.debug("GenericTrap:     " + String.valueOf(pduV1.getGenericTrap()));
				logger.debug("SpecificTrap:    " + String.valueOf(pduV1.getSpecificTrap()));
				logger.debug("SnmpVersion:     v1");
				logger.debug("CommunityString: " + new String(e.getSecurityName()));

			// Build the details for a V2c or v3 TRAP PDU
			} else if (pdu.getType() == PDU.TRAP || pdu.getType() == PDU.INFORM) {

				jsonDetailsObjectBuilder.add("Type", PDU.getTypeString(pdu.getType()))
						.add("ErrorStatus", String.valueOf(pdu.getErrorStatus()))
						.add("ErrorStatusText", String.valueOf(pdu.getErrorStatusText()))
						.add("ErrorIndex", String.valueOf(pdu.getErrorIndex()))
						.add("RequestID", String.valueOf(pdu.getRequestID()))
						.add("MaxRepetitions", String.valueOf(pdu.getMaxRepetitions()))
						.add("NonRepeaters", String.valueOf(pdu.getNonRepeaters()))
						.add("SnmpVersion", "v2c/v3")
						.add("CommunityString", new String(e.getSecurityName()));

				// Build the details for an INFORM PDU
				if (pdu.getType() == PDU.INFORM) {
					logger.debug("SNMP v2c/v3 INFORM RECEIVED");
					logger.debug("Type:            " + PDU.getTypeString(pdu.getType()));

					PDU response = pdu;
					response.setType(PDU.RESPONSE);

					StatusInformation statusInformation = new StatusInformation();
					StateReference<?> ref = e.getStateReference();
					try {
						e.setProcessed(true);
						e.getMessageDispatcher().returnResponsePdu(e.getMessageProcessingModel(), e.getSecurityModel(),
								e.getSecurityName(), e.getSecurityLevel(), response, e.getMaxSizeResponsePDU(), ref,
								statusInformation);
					} catch (MessageException ex) {
						logger.error("Error sending INFORM response", ex);
					}

				} else {
					logger.debug("SNMP v2/v3 TRAP RECEIVED");
					logger.debug("Type:            " + PDU.getTypeString(pdu.getType()));
				}

				logger.debug("ErrorStatus:     " + String.valueOf(pdu.getErrorStatus()));
				logger.debug("ErrorStatusText: " + String.valueOf(pdu.getErrorStatusText()));
				logger.debug("ErrorIndex:      " + String.valueOf(pdu.getErrorIndex()));
				logger.debug("RequestID:       " + String.valueOf(pdu.getRequestID()));
				logger.debug("MaxRepetitions:  " + String.valueOf(pdu.getMaxRepetitions()));
				logger.debug("NonRepeaters:    " + String.valueOf(pdu.getNonRepeaters()));
				logger.debug("SnmpVersion:     v2c/v3");				
				logger.debug("CommunityString: " + new String(e.getSecurityName()));

			}

			// Extract variable bindings from the PDU
			int counter = 0;
			while (varIter.hasNext()) {
				VariableBinding vb = varIter.next();

				jsonDetailsObjectBuilder.add(Integer.toString(counter) + "_OID", vb.getOid().toString())
						.add(Integer.toString(counter) + "_Value", vb.getVariable().toString())
						.add(Integer.toString(counter) + "_SyntaxString", vb.getVariable().getSyntaxString())
						.add(Integer.toString(counter) + "_Syntax", Integer.toString(vb.getVariable().getSyntax()));

				logger.debug(Integer.toString(counter) + "_OID:           " + vb.getOid());
				logger.debug(Integer.toString(counter) + "_Value:         " + vb.getVariable());
				logger.debug(Integer.toString(counter) + "_SyntaxString:  " + vb.getVariable().getSyntaxString());
				logger.debug(Integer.toString(counter) + "_Syntax:        " + Integer.toString(vb.getVariable().getSyntax()));
				counter++;

			}

			// Add details to the JSON payload and place it in a JSON array (which is expected
			// by the Machine Agent HTTP Listener
			jsonObjectBuilder.add("details", jsonDetailsObjectBuilder);
			jsonArr = jsonArrayBuilder.add(jsonObjectBuilder).build();

			sendEvent(jsonArr);
			
			// Adding test code for analytics custom events
			JsonObject jsonAnalyticsObject = null;
			JsonObjectBuilder jsonAnalyticsObjectBuilder = Json.createObjectBuilder();
			
			jsonAnalyticsObjectBuilder.add("method", "TRAP").add("version", "3.0").add("kyle", "true");
			jsonAnalyticsObject = jsonAnalyticsObjectBuilder.build();
			
			logger.debug("jsonAnalyticsObject.toString() = " + jsonAnalyticsObject.toString());
			
			sendAnalyticsEvent(jsonAnalyticsObject.toString());
			
		}
		logger.debug("TRAP END ");
	}

	/**
	 * The sendEvent method is called by the {@link processPdu} method. It sends the
	 * Custom Event to the Machine Agent HTTP listener.
	 */
	private void sendEvent(JsonArray json) {

		logger.debug("JSON Custom Event Body String: " + json.toString());

		String maUrl = "http://" + this.machineAgentHost + ":" + this.machineAgentPort + MA_HTTP_URI;

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(maUrl);
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
				logger.info("SNMP Trap Event published to controller");
			} else {
				logger.error("Unexpected response : " + response);
			}
		} catch (IOException e) {
			logger.error("Error while posting event to controller", e);
		}
	}

	// The sendAnalyticsEvent is called by the {@link processPdu} method. It sends a
	// Custom Event to the Events Service in the SnmpTrap Custom Analytics Event Schema.
	private void sendAnalyticsEvent(String analyticsEventString) {
		
		SnmpTrapReceiverEventsManager snmpTrapReceiverEventsManager = new SnmpTrapReceiverEventsManager(this.configuration.getContext().getEventsServiceDataManager());
		
		try {
			
			snmpTrapReceiverEventsManager.createSchema();
			snmpTrapReceiverEventsManager.publishEvents(analyticsEventString);
		}
		catch (Exception e) {
			logger.error("Error when publishing events", e);
		}
		
	}
	
	
	/**
	 * The SnmpTrapReceiverTask initializes the private member variables for the
	 * SNMP Receiver and validates them.
	 */
	@SuppressWarnings("unchecked")
	public SnmpTrapReceiverTask(MonitorContextConfiguration configuration, MetricWriteHelper metricWriteHelper,
			Map<String, String> server) {

		this.configuration = configuration;
		this.metricWriteHelper = metricWriteHelper;
		this.server = server;
		this.metricPrefix = configuration.getMetricPrefix();
		this.metricList = (List<Map<String, ?>>) configuration.getConfigYml().get(METRICS);
		Map<String, ?> configYaml = (Map<String, ?>) configuration.getConfigYml();

		// Retrieve configurations for the Machine Agent HTTP Listener Host and Port
		Map<String, String> machineAgentConnMaps = (Map<String, String>) configYaml.get("machineAgentConnection");
		this.machineAgentHost = machineAgentConnMaps.get("host");
		this.machineAgentPort = machineAgentConnMaps.get("port");
		logger.debug("YAML machineAgentConnection: " + machineAgentConnMaps);
		logger.debug("YAML Machine Agent Host: " + this.machineAgentHost);
		logger.debug("YAML Machine Agent Port: " + this.machineAgentPort);

		// Retrieve configurations for the SNMP Listener Address and Security settings
		Map<String, String> snmpConnMaps = (Map<String, String>) configYaml.get("snmpConnection");
		logger.debug("YAML snmpConnection: " + snmpConnMaps);

		// Set the member variables from the YAML-provided configurations
		this.snmpListenAddress = snmpConnMaps.get("snmpProtocol") + ":" + snmpConnMaps.get("snmpIP") + "/"
				+ snmpConnMaps.get("snmpPort");
		this.snmpUsername = snmpConnMaps.get("snmpUsername");
		this.snmpAuthProtocol = snmpConnMaps.get("snmpAuthProtocol");
		this.snmpAuthPassPhrase = snmpConnMaps.get("snmpAuthPassPhrase");
		this.snmpPrivacyProtocol = snmpConnMaps.get("snmpPrivacyProtocol");
		this.snmpPrivacyPassPhrase = snmpConnMaps.get("snmpPrivacyPassPhrase");
		logger.debug("YAML SNMP Listen Address = " + this.snmpListenAddress);
		logger.debug("YAML SNMP Username = " + this.snmpUsername);
		logger.debug("YAML SNMP Auth Protocol = " + this.snmpAuthProtocol);
		logger.debug("YAML SNMP Auth Pass Phrase = " + this.snmpAuthPassPhrase);
		logger.debug("YAML SNMP Privacy Protocol = " + this.snmpPrivacyProtocol);
		logger.debug("YAML SNMP Privacy Pass Phrase = " + this.snmpPrivacyPassPhrase);

		AssertUtils.assertNotNull(this.metricList, "The 'metrics' section in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.machineAgentHost,
				"The 'machineAgentHost' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.machineAgentPort,
				"The 'machineAgentPort' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpListenAddress,
				"The 'snmpListenAddress' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpUsername, "The 'snmpUsername' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpAuthProtocol,
				"The 'snmpAuthProtocol' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpAuthPassPhrase,
				"The 'snmpAuthPassPhrase' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpPrivacyProtocol,
				"The 'snmpPrivacyProtocol' value in config.yml is either null or empty");
		AssertUtils.assertNotNull(this.snmpPrivacyPassPhrase,
				"The 'snmpPrivacyPassPhrase' value in config.yml is either null or empty");
	}

	/**
	 * The run method starts the SNMP receiver (which runs continuously) and sends
	 * event metrics (incoming trap count, and outgoing event count) to the Machine
	 * Agent once-per-minute.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		logger.info("Creating new SnmpTrapReceiverTask to listen for SNMP Traps");

		// Start the SNMP Receiver
		this.snmpRun();

		// Initialize counters for the incoming and outgoing event metrics
		incomingTraps = 0;
		outgoingEvents = 0;

		// This Task is running continuously, so we'll manage sending the metrics
		// every minute here manually, once-per-minute.
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

			// After sending minute-frequency metrics, reset counters
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