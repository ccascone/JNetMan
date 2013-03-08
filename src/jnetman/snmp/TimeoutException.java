package jnetman.snmp;

import org.snmp4j.smi.Address;

public class TimeoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Address address;

	public TimeoutException(Address address) {
		this.address = address;
	}

	public String getMessage() {
		return "Request timeout, no response received from "
				+ this.address.toString();

	}

	public Address getAddress() {
		return this.address;
	}

}
