package jnetman.snmp;

import org.apache.log4j.Logger;
import org.snmp4j.smi.VariableBinding;

public class MibHelper {

	SnmpHelper snmpHelper;
	SnmpClient snmpClient;
	Logger logger;

	public MibHelper(SnmpHelper snmpHelper) {
		this.snmpHelper = snmpHelper;
		this.snmpClient = snmpHelper.getSnmpClient();
		logger = Logger.getLogger("snmp.mibHelper."
				+ snmpClient.getNetworkDevice().getName());
		logger.debug("New MIB Helper created");
	}

	public int lookupIfIndex(String ifDescr) throws TimeoutException,
			SnmpErrorException {
		VariableBinding[] vbs = snmpClient.walk(MIB.IfDescr);
		for (VariableBinding vb : vbs) {
			if (vb.toValueString().equals(ifDescr))
				return vb.getOid().last();
		}
		logger.error("IfIndex not found");
		return -1;
	}

}
