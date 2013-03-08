package jnetman.network;

import org.apache.log4j.Logger;

public class Link {

	private String name;
	private Network network;
	private IfCard source;
	private IfCard target;
	protected Logger logger;
	private long nominalSpeed = -1;

	public Link(String name, Network network) {
		this.name = name;
		this.network = network;
		logger = Logger.getLogger("network.link." + name);
	}

	public String getName() {
		return this.name;
	}

	public Network getNetwork() {
		return this.network;
	}

	public IfCard getSource() {
		return this.source;
	}

	public IfCard getTarget() {
		return this.target;
	}

	public void setEndpoint(IfCard ifCard)
			throws LinkAlreadyConnectedException {
		if (this.source == null) {
			this.source = ifCard;
			ifCard.setLink(this);
			logger.debug("Source endpoint attacched >> "
					+ ifCard.getHierarchicalName());
		} else if (this.target == null) {
			this.target = ifCard;
			ifCard.setLink(this);
			logger.debug("Target endpoint attacched >> "
					+ ifCard.getHierarchicalName());
		} else
			throw new LinkAlreadyConnectedException();
	}

	public long getNominalSpeed() throws LinkException {
		if (nominalSpeed > 0)
			return nominalSpeed;

		if (source != null) {
			if (target != null) {
				if (source.getNominalSpeed() < target.getNominalSpeed())
					return source.getNominalSpeed();
				else
					return target.getNominalSpeed();
			}
			return source.getNominalSpeed();
		}
		throw new LinkException("Unable to evaluate nominal speed, link "
				+ name + " has no connected endpoints");
	}

	public void setNominalSpeed(long value) {
		this.nominalSpeed = value;
	}

	public String getDescription() {
		return name + " : " + this.getEndpointsDescription();
	}
	
	public String getEndpointsDescription() {
		String sourceName = "null";
		String targetName = "null";
		if (this.source != null)
			sourceName = source.getHierarchicalName();
		if (this.target != null)
			targetName = target.getHierarchicalName();

		return sourceName + " " + targetName;
	}

	public String toString() {
		return "Link " + name;
	}

}
