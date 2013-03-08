package jnetman.network;

import jnetman.snmp.MIB;
import jnetman.snmp.MibHelper;
import jnetman.snmp.SnmpClient;
import jnetman.snmp.SnmpErrorException;
import jnetman.snmp.SnmpHelper;
import jnetman.snmp.SnmpSyntaxException;
import jnetman.snmp.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

public abstract class Agent {

	protected MibHelper mibHelper;
	protected SnmpHelper snmpHelper;
	protected SnmpClient snmpClient;
	protected Logger logger;

	public Agent(NetworkDevice networkDevice)
			throws AddressException {
		if (networkDevice.getAddress() == null)
			throw new AddressException(
					"No IP address setted for "
							+ networkDevice.getName()
							+ ", you need to set an IP address before calling the agent ");
		this.snmpClient = new SnmpClient(networkDevice);
		this.snmpHelper = new SnmpHelper(this.snmpClient);
		this.mibHelper = new MibHelper(this.snmpHelper);
		logger = Logger.getLogger("network."
				+ StringUtils.uncapitalize(this.getClass().getSimpleName())
				+ "." + networkDevice.getName());
		logger.debug("New agent created");
	}

	public SnmpClient getSnmpClient() {
		return this.snmpClient;
	}

	public SnmpHelper getSnmpHelper() {
		return this.snmpHelper;
	}

	public MibHelper getMibHelper() {
		return this.mibHelper;
	}

	public boolean isResponding() {
		VariableBinding vb = null;
		try {
			vb = this.snmpClient.get(MIB.sysUptime);
			SnmpSyntaxException.checkForExceptions(vb);
			if (!vb.getOid().equals(MIB.sysUptime)) {
				logger.debug("Snmp agent connectivity check FAILED! sysUpTime OID "
						+ MIB.sysUptime
						+ " was requested but agent returned a different OID "
						+ vb.getOid());
				return false;
			} else if (((TimeTicks) vb.getVariable()).toMilliseconds() <= 0) {
				logger.debug("Snmp agent connectivity check FAILED! Illegale value for sysUptime = "
						+ ((TimeTicks) vb.getVariable()).toLong());
				return false;
			}

			logger.debug("Snmp agent connectivity check PASSED, sysUpTime = "
					+ ((TimeTicks) vb.getVariable()).toMilliseconds() + " > 0");
			return true;
		} catch (TimeoutException e) {
			logger.info("Snmp agent connectivity check failed due to request time out");
			return false;
		} catch (SnmpSyntaxException e) {
			logger.info("Snmp agent connectivity check failed due to an SNMP syntax exception retrieving sysUpTime ("
					+ vb.toString() + ")");
			return false;
		} catch (SnmpErrorException e) {
			logger.info("Snmp agent connectivity check failed due to the following SNMP Error >> "
					+ e.getMessage());
			return false;
		}
	}

}
