package jnetman.snmp;

public class SnmpErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1304907512926256963L;

	public SnmpErrorException(String errorStatusText) {
		super(errorStatusText);
	}

}
