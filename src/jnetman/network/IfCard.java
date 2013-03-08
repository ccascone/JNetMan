package jnetman.network;

import java.net.InetAddress;

import org.apache.log4j.Logger;

public class IfCard {

	private String name;
	private Link link;
	private Node node;
	private InetAddress address;
	private short addrPrefixLenght = -1;
	protected Logger logger;
	private IfCardAgent manager;

	private long nominalSpeed = -1;
	private long peakSpeed = -1;

	protected IfCard(String name, Node node) {
		this.name = name;
		this.node = node;
		logger = Logger.getLogger("network.ifCard." + getHierarchicalName());
		logger.debug("New interface card created");
	}

	public String getName() {
		return this.name;
	}

	public String getHierarchicalName() {
		return this.node.getName() + "." + this.name;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
		logger.debug("IP address updated >> " + address.getHostAddress());
	}

	public short getAddrPrefixLenght() {
		return addrPrefixLenght;
	}

	public void setAddrPrefixLenght(short addrPrefixLenght) {
		this.addrPrefixLenght = addrPrefixLenght;
		logger.debug("Network prefix length updated >> " + addrPrefixLenght);
	}

	public Node getNode() {
		return this.node;
	}

	protected void setRouter(Node node) {
		this.node = node;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Link getLink() {
		if (this.hasLink())
			return this.link;
		else
			return null;
	}

	public boolean hasLink() {
		if (link != null)
			return true;
		else
			return false;
	}

	public IfCard getLinkEndpoint() {
		if (this.hasLink()) {
			if (this.link.getSource() == this)
				return this.link.getTarget();
			else
				return this.link.getSource();
		} else
			return null;
	}

	public String getDescription() {
		String text = name;
		if (this.address != null)
			text += " : " + address.getHostAddress() + "/" + addrPrefixLenght
					+ " @ ";
		else
			text += " : null @ ";
		if (hasLink())
			text += link.getName();
		else
			text += "null";
		return text;
	}

	public String toString() {
		return "IfCard " + name;
	}

	public long getNominalSpeed() {
		return this.nominalSpeed;
	}

	public void setNominalSpeed(long speed) {
		this.nominalSpeed = speed;
		logger.debug("NominalSpeed updated >> " + speed);
	}

	public long getPeakSpeed() {
		return peakSpeed;
	}

	public long setPeakSpeed() {
		return peakSpeed;
	}

	public void updatePeakSpeed(long peakSpeed) {
		if (peakSpeed > this.peakSpeed) {
			this.peakSpeed = peakSpeed;
			logger.debug("PeakSpeed updated >> " + peakSpeed);
		}
	}

	public IfCardAgent getAgent() throws AddressException {
		if (this.manager == null)
			this.manager = new IfCardAgent(this);
		return this.manager;
	}
}
