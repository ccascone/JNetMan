package jnetman.snmp;

import java.io.IOException;
import java.util.Vector;

import jnetman.network.AddressException;
import jnetman.network.NetworkDevice;
import jnetman.session.SnmpPref;

import org.apache.log4j.Logger;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeListener;
import org.snmp4j.util.TreeUtils;

public class SnmpClient implements PDUFactory {

	private Snmp snmpInstance;
	private NetworkDevice targetDevice;

	private static int requestID = 1;
	private Logger logger;

	/**
	 * Create a new SnmpClient for the passed NetworkDevice.
	 * 
	 * @param targetDevice
	 *            NetworkDevice that will be used for SNMP message exchange.
	 */
	public SnmpClient(NetworkDevice targetDevice) {
		this.targetDevice = targetDevice;

		// Create a new logger with name jnetman.snmp.device_name
		logger = Logger.getLogger("snmp.snmpClient." + targetDevice.getName());

		// If activated SNMP4J will show a huge amount of low level debug info
		if (SnmpPref.isSnmp4jLogEnabled())
			LogFactory.setLogFactory(new Log4jLogFactory());

		try {
			DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

			// Creates Snmp istance for that transport channel
			snmpInstance = new Snmp(transport);
			logger.debug("New SNMP Client crated");

			// Creates v3 SNMP USM
			USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
					MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);

			// Adds 'jnetman' usm using MD5 authentication and DES encryption
			UsmUser jnetmanUser = new UsmUser(
					new OctetString(SnmpPref.getUser()), AuthMD5.ID,
					new OctetString(SnmpPref.getPassword()), PrivDES.ID,
					new OctetString(SnmpPref.getPassword()));
			snmpInstance.getUSM().addUser(new OctetString(SnmpPref.getUser()),
					jnetmanUser);
			logger.debug("New USM User added >> " + jnetmanUser.getSecurityName());

			// Enables listening for incoming SNMP packet
			transport.listen();

		} catch (IOException e) {
			logger.fatal(
					"IOException while creating a new DefaultUdpTransportMapping()",
					e);
			System.exit(-1);
		}
	}

	public NetworkDevice getNetworkDevice() {
		return this.targetDevice;
	}

	/**
	 * Returns a destination target to be used for a new SNMP message. The
	 * target returned will use SNMP v3 protocol with authentication and privacy
	 * enabled.
	 * 
	 * @return v3 authPriv Target
	 * @throws AddressException
	 */
	private Target getV3AuthPrivTarget() {

		String hostAddress = null;
		try {
			hostAddress = targetDevice.getAddress().getHostAddress();
		} catch (AddressException e) {
			logger.fatal("Unable to get the IP address from the NetworkDevice",
					e);
			System.exit(-1);
		}

		// set the address using the format 'udp:192.168.1.1/161'
		String address = String.format("udp:%s/%d", hostAddress,
				SnmpPref.getPort());
		Address targetAddress = GenericAddress.parse(address);

		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setRetries(SnmpPref.getMaxRetries());
		target.setTimeout(SnmpPref.getTimeout());
		target.setVersion(SnmpConstants.version3);
		target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		target.setSecurityName(new OctetString(SnmpPref.getUser()));

		return target;
	}

	/**
	 * Send a new GET request for multiple OIDs.
	 * 
	 * @param oids
	 *            Array of OID for the request.
	 * @return ResponseEvent for the request only if no timeout has occurred.
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding[] get(OID oids[]) throws TimeoutException,
			SnmpErrorException {
		return this.packPDUAndSend(oids, PDU.GET);
	}

	/**
	 * Send a new GET request for a single OID.
	 * 
	 * @param oid
	 *            OID of the request.
	 * @return ResponseEvent for the request only if no timeout has occurred.
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding get(OID oid) throws TimeoutException,
			SnmpErrorException {
		return this.packPDUAndSend(new OID[] { new OID(oid) }, PDU.GET)[0];
	}

	/**
	 * Send a new GETNEXT request for multiple OIDs.
	 * 
	 * @param oids
	 *            Array of OID for the request.
	 * @return ResponseEvent for the request only if no timeout has occurred.
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding[] getNext(OID oids[]) throws TimeoutException,
			SnmpErrorException {
		return this.packPDUAndSend(oids, PDU.GETNEXT);
	}

	/**
	 * Send a new GETNEXT request for a single OID
	 * 
	 * @param oid
	 *            OID of the request
	 * @return ResponseEvent for the request only if no timeout has occurred
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding getNext(OID oid) throws TimeoutException,
			SnmpErrorException {
		return this.packPDUAndSend(new OID[] { new OID(oid) }, PDU.GETNEXT)[0];
	}

	/**
	 * Send a new SET request for a single OID/value pair
	 * 
	 * @param oid
	 *            OID of the value to write
	 * @param value
	 *            New value to write
	 * @return ResponseEvent for the request only if no timeout has occurred
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding set(OID oid, Variable value)
			throws TimeoutException, SnmpErrorException {
		return this.packPDUAndSend(new VariableBinding[] { new VariableBinding(
				oid, value) }, PDU.SET)[0];
	}

	/**
	 * Send a new SET request for a single VariableBinding
	 * 
	 * @param vb
	 *            VariableBinding of the request
	 * @return ResponseEvent for the request only if no timeout has occurred
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding set(VariableBinding vb) throws TimeoutException,
			SnmpErrorException {
		return this.packPDUAndSend(new VariableBinding[] { vb }, PDU.SET)[0];
	}

	/**
	 * Send a new SET request for multiple VariableBindings
	 * 
	 * @param vb
	 *            Array of VariableBinding of the request
	 * @return ResponseEvent for the request only if no timeout has occurred
	 * @throws TimeoutException
	 *             If request has timed out
	 */
	public VariableBinding[] set(VariableBinding[] vbs)
			throws TimeoutException, SnmpErrorException {
		return this.packPDUAndSend(vbs, PDU.SET);
	}

	public VariableBinding[] walk(OID oid) {

		logger.debug("Starting walk at OID " + oid.toString() + "...");

		final Vector<VariableBinding> snapshot = new Vector<VariableBinding>();

		final WalkCounts counts = new WalkCounts();
		final long startTime = System.nanoTime();

		TreeUtils treeUtils = new TreeUtils(snmpInstance, this);

		TreeListener treeListener = new TreeListener() {

			private boolean finished;

			public boolean next(TreeEvent e) {
				counts.requests++;

				if (e.getVariableBindings() != null) {
					VariableBinding[] vbs = e.getVariableBindings();
					counts.objects += vbs.length;
					for (VariableBinding vb : vbs)
						snapshot.add(vb);
				}
				return true;
			}

			public void finished(TreeEvent e) {
				if ((e.getVariableBindings() != null)
						&& (e.getVariableBindings().length > 0))
					next(e);

				logger.debug("Walk completed in "
						+ (System.nanoTime() - startTime) / 1000000 + " ms, "
						+ counts.objects + " objects received in "
						+ counts.requests + " requests");

				if (e.isError())
					logger.debug("The following error occurred during walk: "
							+ e.getErrorMessage());

				finished = true;

				synchronized (this) {
					this.notify();
				}
			}

			public boolean isFinished() {
				return finished;
			}

		};
		synchronized (treeListener) {
			treeUtils
					.getSubtree(getV3AuthPrivTarget(), oid, null, treeListener);
			try {
				treeListener.wait();
			} catch (InterruptedException ex) {
				logger.debug("Tree retrieval interrupted: " + ex.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		return snapshot.toArray(new VariableBinding[snapshot.size()]);
	}

	public VariableBinding[] packPDUAndSend(OID[] oids, int pduType)
			throws TimeoutException, SnmpErrorException {

		PDU pdu = new ScopedPDU();
		for (OID oid : oids)
			pdu.add(new VariableBinding(oid));

		pdu.setType(pduType);

		return this.send(pdu);
	}

	public VariableBinding[] packPDUAndSend(VariableBinding[] vbs, int pduType)
			throws TimeoutException, SnmpErrorException {

		PDU pdu = new ScopedPDU();
		pdu.addAll(vbs);

		pdu.setType(pduType);

		return this.send(pdu);
	}

	private synchronized VariableBinding[] send(PDU pdu)
			throws TimeoutException, SnmpErrorException {

		ResponseEvent event = null;

		/*
		 * Well, it's now time to send a new SNMP message! Set the RequestID of
		 * the PDU and increment the counter for the next operation.
		 */
		pdu.setRequestID(new Integer32(requestID));
		requestID++;

		/*
		 * The PDU is now ready to be sent
		 */
		Target target = getV3AuthPrivTarget();
		// Target target = getV3AuthNoPrivTarget();
		/*
		 * Some debug about the request that is going to be sent.
		 */
		logger.debug("Sending request to " + target.getAddress() + " >> " + pdu);

		/*
		 * Calculate the time elapsed between the beginning of request
		 * transmission and the end of the response reception, for debug
		 * purpose.s
		 */
		long startTime = System.nanoTime();
		try {
			event = snmpInstance.send(pdu, target, null);
		} catch (IOException e) {
			logger.fatal("IOException while sending a new SNMP message", e);
			System.exit(-1);
		}
		long timeElapsed = System.nanoTime() - startTime;

		/*
		 * If response != null means that no timeout has occurred and a response
		 * was successfully delivered.
		 */
		if (event.getResponse() != null) {

			PDU response = event.getResponse();
			/*
			 * Some debug about the response received
			 */
			logger.debug("Response received from " + event.getPeerAddress()
					+ " in " + timeElapsed / 1000000 + " ms >> " + response);

			/*
			 * Check for common SNMP errors due to failed request
			 */
			if (response.getErrorStatus() == PDU.noError) {

				/*
				 * Let's finally return the event, this could still contain a
				 * SnmpSyntaxException (noSuchInstance, noSuchObject or
				 * endOfMibView) but we don't care throwing it now. The client
				 * user could be interested in doing it.
				 */
				return response.toArray();

			} else {
				/*
				 * SNMP error has occurred.
				 */
				/*
				 * TODO Net-Snmp REPORT for failed authentication
				 */
				logger.error("SNMP ERROR >> " + response.getErrorStatusText());
				throw new SnmpErrorException(response.getErrorStatusText());
			}
		}

		/*
		 * If here means that response == null, REQUEST TIME OUT!
		 */
		logger.error("Request TIMEOUT! No response received from "
				+ target.getAddress());
		throw new TimeoutException(target.getAddress());

	}

	public PDU createPDU(Target target) {
		return new ScopedPDU();
	}

	private class WalkCounts {
		public int requests;
		public int objects;
	}

}
