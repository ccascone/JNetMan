package jnetman.snmp;

import java.util.Arrays;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;

public class TimedValues {

	private Variable[] variables;
	private long millis;

	public TimedValues(OID[] oids, SnmpHelper mibHelper)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		OID[] timedOids = Arrays.copyOf(oids, oids.length + 1);
		// add a sysUpTime oid request as last
		timedOids[oids.length] = new OID(MIB.sysUptime);
		Variable[] timedVariables = mibHelper.getVariable(timedOids);
		// last one is sysUpTime
		millis = ((TimeTicks) timedVariables[oids.length]).toMilliseconds();
		variables = Arrays.copyOf(timedVariables, oids.length);
	}

	public long getMillis() {
		return this.millis;
	}

	public Variable[] getValues() {
		return this.variables;
	}

	public long getMillisDiff(TimedValues finalValues) {
		return finalValues.getMillis() - this.getMillis();
	}

}