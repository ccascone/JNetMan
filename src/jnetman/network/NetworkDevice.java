package jnetman.network;

import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class NetworkDevice {

	private String name;
	private Network network;
	private InetAddress address;
	protected Logger logger;

	protected NetworkDevice(String name, Network network) {
		this.name = name;
		this.network = network;
		this.logger = Logger.getLogger("network."
				+ StringUtils.uncapitalize(this.getClass().getSimpleName())
				+ "." + name);
		logger.debug("New " + this.getClass().getSimpleName() + " created");
	}

	/**
	 * Returns the name of the node. This is the name that will be used to refer
	 * to the node when called from other methods inside the NetworkManager.
	 * 
	 * @return the name of the node in String format
	 */
	public String getName() {
		return this.name;
	}

	protected void setName(String name) {
		if (this.network != null)
			throw new RuntimeException(
					"It is forbidden to change the name once the "
							+ "NetworkDevice has been added to the Network");
		this.name = name;
	}

	/**
	 * Returns the IP address of the node.
	 * 
	 * @return IP address of the node in InetAddress format
	 * @throws AddressException
	 *             If no address is explicitly
	 */
	public InetAddress getAddress() throws AddressException {
		return this.address;
	}

	/**
	 * Assign an IP address to the node.
	 * 
	 * @param address
	 *            The IP address to assign in InetAddress format
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
		logger.debug("IP address updated >> " + this.address.getHostAddress());
	}

	/**
	 * Returns the Network object to which the node belongs.
	 * 
	 * @return The network to which the node belongs as a Network object
	 */
	public Network getNetwork() {
		return this.network;
	}

	protected void setNetwork(Network network) {
		if (this.name == null)
			throw new RuntimeException(
					"You must assign a name to this NetworkDevice before "
							+ "adding it to the Network");
		this.network = network;
	}

	public String toString() {
		if (this.address != null)
			return this.getClass().getCanonicalName() + " " + name + " : "
					+ address.getHostAddress();
		else
			return this.getClass().getCanonicalName() + " " + name + " : null";
	}

	public abstract Agent getAgent() throws AddressException;

}
