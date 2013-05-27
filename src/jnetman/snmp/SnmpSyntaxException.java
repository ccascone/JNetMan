package jnetman.snmp;

import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class SnmpSyntaxException extends Exception {

	private VariableBinding vb;
	private Variable variable;

	public SnmpSyntaxException(VariableBinding vb) {
		this.vb = vb;
		this.variable = vb.getVariable();
	}

	public SnmpSyntaxException(Variable variable) {
		this.variable = variable;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8067097283760805458L;

	/**
	 * Check a ResponseEvent for a syntax exception (noSuchInstance,
	 * noSuchObject, endOfMibView). If multiple variable bindings are contained
	 * in the response a SnmpSyntaxException will be thrown at the first syntax
	 * exception encountered.
	 * 
	 * @param event
	 *            ResponseEvent to be checked
	 * @throws SnmpSyntaxException
	 *             If the syntax one of the variable included in the response
	 *             equals one of the following:
	 *             <ul>
	 *             <li>noSuchInstance</li>
	 *             <li>noSuchObject</li>
	 *             <li>endOfMibView</li> </li>
	 */
	public static void checkForException(ResponseEvent event)
			throws SnmpSyntaxException {
		SnmpSyntaxException.checkForExceptions(event.getResponse().toArray());
	}

	/**
	 * Check an array of variable bindings for a syntax exception
	 * (noSuchInstance, noSuchObject, endOfMibView). A SnmpSyntaxException will
	 * be thrown at the first syntax exception encountered.
	 * 
	 * @param vbs
	 *            array of variable bindings to be checked
	 * @throws SnmpSyntaxException
	 *             If the syntax one of the variable equals one of the
	 *             following:
	 *             <ul>
	 *             <li>noSuchInstance</li>
	 *             <li>noSuchObject</li>
	 *             <li>endOfMibView</li> </li>
	 */
	public static void checkForExceptions(VariableBinding[] vbs)
			throws SnmpSyntaxException {
		for (VariableBinding vb : vbs)
			SnmpSyntaxException.checkForExceptions(vb);
	}

	/**
	 * Check a variable binding for a syntax exception (noSuchInstance,
	 * noSuchObject, endOfMibView).
	 * 
	 * @param vb
	 *            variable binding to be checked
	 * @throws SnmpSyntaxException
	 *             if the syntax of this variable equals one of the following:
	 *             <ul>
	 *             <li>noSuchInstance</li>
	 *             <li>noSuchObject</li>
	 *             <li>endOfMibView</li> </li>
	 */
	public static void checkForExceptions(VariableBinding vb)
			throws SnmpSyntaxException {
		if(vb.getVariable().isException())
			throw new SnmpSyntaxException(vb);
	}

	/**
	 * Check a variable for a syntax exception (noSuchInstance, noSuchObject,
	 * endOfMibView).
	 * 
	 * @param variable
	 *            variable to be checked
	 * @throws SnmpSyntaxException
	 *             if the syntax of this variable equals one of the following:
	 *             <ul>
	 *             <li>noSuchInstance</li>
	 *             <li>noSuchObject</li>
	 *             <li>endOfMibView</li> </li>
	 */
	public static void checkForExceptions(Variable variable)
			throws SnmpSyntaxException {
		if (variable.isException())
			throw new SnmpSyntaxException(variable);
	}

	/**
	 * @return <code>true</code> if this exception has been generated by a
	 *         noSuchInstance SNMP syntax exception
	 */
	public boolean isNoSuchInstance() {
		if (this.variable.getSyntax() == SMIConstants.EXCEPTION_NO_SUCH_INSTANCE)
			return true;
		else
			return false;
	}

	/**
	 * @return <code>true</code> if this exception has been generated by a
	 *         noSuchObject SNMP syntax exception
	 */
	public boolean isNoSuchObject() {
		if (this.variable.getSyntax() == SMIConstants.EXCEPTION_NO_SUCH_OBJECT)
			return true;
		else
			return false;
	}

	/**
	 * @return <code>true</code> if this exception has been generated by a
	 *         isEndOfMibView SNMP syntax exception
	 */
	public boolean isEndOfMibView() {
		if (this.variable.getSyntax() == SMIConstants.EXCEPTION_END_OF_MIB_VIEW)
			return true;
		else
			return false;
	}

	public String getMessage() {
		if (vb != null)
			return vb.toString();
		else
			return variable.getSyntaxString();
	}

}