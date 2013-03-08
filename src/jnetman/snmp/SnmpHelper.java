package jnetman.snmp;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.snmp4j.smi.AbstractVariable;
import org.snmp4j.smi.AssignableFromInteger;
import org.snmp4j.smi.AssignableFromLong;
import org.snmp4j.smi.AssignableFromString;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class SnmpHelper {

	private SnmpClient snmpClient;
	protected Logger logger;

	public SnmpHelper(SnmpClient snmpClient) {
		this.snmpClient = snmpClient;
		logger = Logger.getLogger("snmp.snmpHelper."
				+ snmpClient.getNetworkDevice().getName());
		logger.debug("New Snmp helper created");
	}

	public SnmpClient getSnmpClient() {
		return this.snmpClient;
	}

	public Variable[] getVariable(OID[] oids) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		VariableBinding[] vbs = this.snmpClient.get(oids);
		Variable[] vs = new Variable[vbs.length];
		for (int i = 0; i < vbs.length; i++) {
			SnmpSyntaxException.checkForExceptions(vbs[i]);
			vs[i] = vbs[i].getVariable();
		}
		return vs;
	}

	public Variable getVariable(OID oid) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		return getVariable(new OID[] { oid })[0];
	}

	public TimedValues getAsTimedValues(OID[] oids) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		return new TimedValues(oids, this);
	}

	public int getInt(OID oid) throws TimeoutException, SnmpErrorException,
			SnmpSyntaxException {
		return this.getVariable(oid).toInt();
	}

	public int[] getInt(OID[] oids) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = getVariable(oids);
		int[] ints = new int[vars.length];
		for (int i = 0; i < vars.length; i++)
			ints[i] = vars[i].toInt();
		return ints;
	}

	public long getLong(OID oid) throws TimeoutException, SnmpErrorException,
			SnmpSyntaxException {
		return this.getVariable(oid).toLong();
	}

	public long[] getLong(OID[] oids) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = getVariable(oids);
		long[] longs = new long[vars.length];
		for (int i = 0; i < vars.length; i++)
			longs[i] = vars[i].toLong();
		return longs;
	}

	public String getString(OID oid) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		return this.getVariable(oid).toString();
	}

	public String[] getString(OID[] oids) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = getVariable(oids);
		String[] strings = new String[vars.length];
		for (int i = 0; i < vars.length; i++)
			strings[i] = vars[i].toString();
		return strings;
	}

	public boolean[] setVariableBinding(VariableBinding[] vbs)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		VariableBinding[] resVbs = snmpClient.set(vbs);
		SnmpSyntaxException.checkForExceptions(resVbs);

		boolean[] res = new boolean[vbs.length];

		for (int i = 0; i < vbs.length; i++) {
			SnmpSyntaxException.checkForExceptions(resVbs[i]);
			if (resVbs[i].getOid().equals(vbs[i].getOid())
					&& resVbs[i].getVariable().equals(vbs[i].getVariable()))
				res[i] = true;
			else
				res[i] = false;
		}
		return res;
	}

	public boolean setVariableBinding(VariableBinding vb)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		return setVariableBinding(new VariableBinding[] { vb })[0];
	}

	public boolean[] setVariable(OID[] oids, Variable[] values)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		if (oids.length != values.length)
			throw new IllegalArgumentException(
					"oids and values arrays must have same length");
		VariableBinding[] vbs = new VariableBinding[oids.length];
		for (int i = 0; i < vbs.length; i++) {
			vbs[i] = new VariableBinding(oids[i], values[i]);
		}
		return setVariableBinding(vbs);
	}

	public boolean setVariable(OID oid, Variable value)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		return setVariable(new OID[] { oid }, new Variable[] { value })[0];
	}

	public boolean[] setInt(OID[] oids, int[] values, int[] smiSyntaxs)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = intToVariable(values, smiSyntaxs);
		return setVariable(oids, vars);
	}

	public boolean setInt(OID oid, int value, int smiSyntax)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		return setInt(new OID[] { oid }, new int[] { value },
				new int[] { smiSyntax })[0];
	}

	public boolean[] setLong(OID[] oids, long[] values, int[] smiSyntaxs)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = longToVariable(values, smiSyntaxs);
		return setVariable(oids, vars);
	}

	public boolean setLong(OID oid, long value, int smiSyntax)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		return setLong(new OID[] { oid }, new long[] { value },
				new int[] { smiSyntax })[0];
	}

	public boolean[] setString(OID[] oids, String[] values, int[] smiSyntaxs)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		Variable[] vars = stringToVariable(values, smiSyntaxs);
		return setVariable(oids, vars);
	}

	public boolean setString(OID oid, String value, int smiSyntax)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		return setString(new OID[] { oid }, new String[] { value },
				new int[] { smiSyntax })[0];
	}

	public static Variable stringToVariable(String value, int smiSyntax) {
		Variable var = AbstractVariable.createFromSyntax(smiSyntax);
		if (var instanceof AssignableFromString)
			((AssignableFromString) var).setValue(value);
		else
			throw new IllegalArgumentException(
					"Unsupported conversion from String to "
							+ var.getSyntaxString());
		return var;
	}

	public static Variable[] stringToVariable(String[] values, int[] smiSyntaxs) {
		if (values.length != smiSyntaxs.length)
			throw new IllegalArgumentException(
					"values and smiSyntaxs arrays must have smae length");
		Variable[] vars = new Variable[values.length];
		for (int i = 0; i < values.length; i++) {
			vars[i] = stringToVariable(values[i], smiSyntaxs[i]);
		}
		return vars;
	}

	public static Variable intToVariable(int i, int smiSyntax) {
		Variable var = AbstractVariable.createFromSyntax(smiSyntax);
		if (var instanceof AssignableFromInteger)
			((AssignableFromInteger) var).setValue(i);
		else
			throw new IllegalArgumentException(
					"Unsupported conversion from int to "
							+ var.getSyntaxString());
		return var;
	}

	public static Variable[] intToVariable(int[] values, int[] smiSyntaxs) {
		if (values.length != smiSyntaxs.length)
			throw new IllegalArgumentException(
					"values and smiSyntaxs arrays must have smae length");
		Variable[] vars = new Variable[values.length];
		for (int i = 0; i < values.length; i++) {
			vars[i] = intToVariable(values[i], smiSyntaxs[i]);
		}
		return vars;
	}

	public static Variable longToVariable(long l, int smiSyntax) {
		Variable var = AbstractVariable.createFromSyntax(smiSyntax);
		if (var instanceof AssignableFromLong)
			((AssignableFromLong) var).setValue(l);
		else
			throw new IllegalArgumentException(
					"Unsupported conversion from long to "
							+ var.getSyntaxString());
		return var;
	}

	public static Variable[] longToVariable(long[] values, int[] smiSyntaxs) {
		if (values.length != smiSyntaxs.length)
			throw new IllegalArgumentException(
					"values and smiSyntaxs arrays must have smae length");
		Variable[] vars = new Variable[values.length];
		for (int i = 0; i < values.length; i++) {
			vars[i] = longToVariable(values[i], smiSyntaxs[i]);
		}
		return vars;
	}

	public Table getTable(OID tableOid) {
		logger.trace("Table retrieval started");

		VariableBinding[] vbs = snmpClient.walk(tableOid);

		Table table = new Table();

		int[] subIdxArr;
		int[] colIdArr;
		OID subIdxOid;
		OID colIdOid;

		for (VariableBinding vb : vbs) {
			if (vb.getOid().leftMostCompare(tableOid.size(), tableOid) != 0) {
				logger.warn("The following OID doesn't seems to belong to table "
						+ vb);
				continue;
			}
			// extract the last part of the OID, the index
			subIdxArr = Arrays.copyOfRange(vb.getOid().toIntArray(),
					tableOid.size() + 2, vb.getOid().size());
			subIdxOid = new OID(subIdxArr);
			// extract the first part of the OID, the column
			colIdArr = Arrays.copyOfRange(vb.getOid().toIntArray(), 0,
					tableOid.size() + 2);
			colIdOid = new OID(colIdArr);
			logger.trace("Index = " + subIdxOid + "; Column = " + colIdOid);
			table.putVariable(subIdxOid, colIdOid, vb.getVariable());
		}

		return table;
	}

	public static int castToInt(Variable variable) {
		switch (variable.getSyntax()) {
		case SMIConstants.SYNTAX_INTEGER:
			return ((Integer32) variable).toInt();
		default:
			throw new IllegalArgumentException("Unsupported cast from "
					+ variable.getSyntaxString() + " to int");
		}
	}

	public static int[] castToInt(Variable[] variables) {
		int[] ints = new int[variables.length];
		for (int i = 0; i < variables.length; i++)
			ints[i] = SnmpHelper.castToInt(variables[i]);

		return ints;
	}

	public static long castToLong(Variable variable) {
		switch (variable.getSyntax()) {
		case SMIConstants.SYNTAX_GAUGE32:
			return ((Gauge32) variable).toLong();
		case SMIConstants.SYNTAX_COUNTER32:
			return ((Counter32) variable).toLong();
		case SMIConstants.SYNTAX_COUNTER64:
			return ((Counter64) variable).toLong();
		case SMIConstants.SYNTAX_TIMETICKS:
			return ((TimeTicks) variable).toLong();
		default:
			throw new IllegalArgumentException("Unsupported cast from "
					+ variable.getSyntaxString() + " to long");
		}
	}

	public static long[] castToLong(Variable[] variables) {
		long[] longs = new long[variables.length];
		for (int i = 0; i < variables.length; i++)
			longs[i] = SnmpHelper.castToLong(variables[i]);

		return longs;
	}

	public static String castToString(Variable variable) {
		return variable.toString();
	}

	public static String[] castToString(Variable[] variables) {
		String[] strings = new String[variables.length];
		for (int i = 0; i < variables.length; i++)
			strings[i] = SnmpHelper.castToString(variables[i]);

		return strings;
	}

	public static OID[] stringsToOIDs(String[] stringOids) {
		OID[] oids = new OID[stringOids.length];
		for (int i = 0; i < stringOids.length; i++)
			oids[i] = new OID(stringOids[i]);
		return oids;
	}

}
