package jnetman.snmp;

import java.io.IOException;
import java.net.UnknownHostException;

import jnetman.session.SnmpPref;

import org.apache.log4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class SnmpTrapReceiver implements CommandResponder {

	// initialize Log4J logging
	static Logger logger = Logger.getLogger("snmp.snmpTrapReceiver");

	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;

	private int n = 0;
	private long start = -1;

	public SnmpTrapReceiver() {
		logger.debug("Created");
	}

	private void init() throws UnknownHostException, IOException {
		threadPool = ThreadPool.create("Trap", 4);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());

		listenAddress = GenericAddress.parse("udp:0.0.0.0/"
				+ SnmpPref.getTrapsPort());
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(
				(UdpAddress) listenAddress);
		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
				MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		snmp.listen();
		logger.debug("Listening for traps on "
				+ transport.getListenAddress().toString());
	}

	public void run() {
		try {
			init();
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void processPdu(CommandResponderEvent event) {
		if (start < 0) {
			start = System.currentTimeMillis() - 1;
		}
		logger.info("TRAP received >> " + event.getPDU().toString());
		n++;
		if ((n % 100 == 1)) {
			logger.info("Some statistics, processing  "
					+ (n / (double) (System.currentTimeMillis() - start))
					* 1000 + " trap/s, total " + n);
		}
	}
}
