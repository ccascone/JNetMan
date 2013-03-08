package jnetman.network;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents the NetworkManager abstraction for a node. A node could
 * be a router as well as a simple host. A node is defined by its name and its
 * network interfaces.
 * 
 * @author Carmelo Cascone
 * 
 */
public class Node extends NetworkDevice {

	private Map<String, IfCard> ifCards;
	protected NodeAgent agent;

	public Node(String name, Network network) {
		super(name, network);
		this.ifCards = new HashMap<String, IfCard>();
	}

	/**
	 * Returns the IP address of the node. If no address is explicitly set the
	 * address of a random interface will be returned.
	 * 
	 * @return IP address of the node in InetAddress format
	 * @throws AddressException
	 *             If no address is explicitly set and there are no interfaces
	 *             declared for the node
	 */
	public InetAddress getAddress() throws AddressException {
		if (super.getAddress() != null)
			return super.getAddress();
		else if (!this.ifCards.isEmpty()) {
			for (IfCard ifCard : this.ifCards.values())
				if (ifCard.getAddress() != null) {
					super.setAddress(ifCard.getAddress());
					logger.debug("No explicit address declared, using "
							+ super.getAddress().getHostAddress()
							+ " from interface " + ifCard.getName());
					return super.getAddress();
				}
		}
		throw new AddressException(
				"Unable to get the IP address of "
						+ this.getName()
						+ ", there are no InterfaceCards specified for this node. "
						+ "You need to add at least one IfCard to get a valid IP address.");
	}

	/**
	 * Returns a network interface defined for this node
	 * 
	 * @param name
	 *            The name of the network interface to get
	 * @return The network interface as an IfCard object
	 */
	public IfCard getInterfaceCard(String name) {
		return this.ifCards.get(name);
	}

	/**
	 * Add a network interface to the node. This method is protected and so
	 * should be used only inside the NetworkManager package. See also
	 * createInterfaceCard.
	 * 
	 * @param ifCard
	 *            IfCard to be added
	 * @throws DuplicateElementException
	 *             If a network interface with the same name already exist for
	 *             this node
	 */
	protected void addInterfaceCard(IfCard ifCard)
			throws DuplicateElementException {
		if (this.ifCards.containsKey(ifCard.getName()))
			throw new DuplicateElementException("A network interface named '"
					+ ifCard.getName() + "' already exist for the node "
					+ this.getName());
		ifCard.setRouter(this);
		this.ifCards.put(ifCard.getName(), ifCard);
		logger.debug("New interface card added >> " + ifCard.getName());
	}

	/**
	 * Create a network interface for the node.
	 * 
	 * @param name
	 *            Name of the network interface to be crated
	 * @return The network interface created as an IfCard object
	 * @throws DuplicateElementException
	 *             If a network interface with the same name already exist for
	 *             this node
	 */
	public IfCard createInterfaceCard(String name)
			throws DuplicateElementException {
		if (this.ifCards.containsKey(name))
			throw new DuplicateElementException("An interfaceCard named '"
					+ name + "' already exist");

		IfCard newIfCard = new IfCard(name, this);
		this.addInterfaceCard(newIfCard);
		return newIfCard;
	}

	/**
	 * Returns all the network interfaces of this node
	 * 
	 * @return A Collection of network interfaces as IfCard objects
	 */
	public Collection<IfCard> getIfCards() {
		return this.ifCards.values();
	}

	/**
	 * Check if this node is connected with the specified node. A node is
	 * connected with another node when at least one link exists between the two
	 * nodes and it's connected to the network interfaces of both nodes.
	 * 
	 * @param node
	 *            The node for which to check the connection
	 * @return true if it's connected, false elsewhere.
	 */
	public boolean isConnectedWith(Node node) {
		if (this.ifCards.isEmpty())
			return false;
		else
			for (IfCard ifCard : this.getIfCards())
				if (ifCard.getLinkEndpoint() != null
						&& ifCard.getLinkEndpoint().getNode() == node)
					return true;
		return false;
	}

	/**
	 * Returns all the nodes connected with this nodes
	 * 
	 * @return a Collection of nodes connected with this node as a Node object
	 */

	public Collection<Node> getNeighbours() {
		Vector<Node> neighbours = new Vector<Node>();
		if (!this.ifCards.isEmpty()) {
			for (IfCard ifCard : this.ifCards.values()) {
				if (ifCard.hasLink() && ifCard.getLinkEndpoint() != null) {
					neighbours.add(ifCard.getLinkEndpoint().getNode());
				}
			}
		}
		return neighbours;
	}

	/**
	 * Returns all the links that connect this node with the specified node
	 * 
	 * @param node
	 *            The other endpoint of the links
	 * @return A Collection of links in as a Link object.
	 * @throws NotConnectedException
	 *             If this node is not connected with the specified node (no
	 *             links exists between the two nodes)
	 */

	public Collection<Link> getAllLinks(Node node) throws NotConnectedException {
		if (this.isConnectedWith(node)) {
			Vector<Link> links = new Vector<Link>();
			for (IfCard ifCard : this.getIfCards()) {
				if (ifCard.getLinkEndpoint() != null
						&& ifCard.getLinkEndpoint().getNode() == node)
					links.add(ifCard.getLink());
			}
			return links;
		} else
			throw new NotConnectedException();
	}

	/**
	 * Returns a a textual description of the node, intended as the node name
	 * and a list of its network interfaces
	 * 
	 * @return a description of the node in a string format
	 */
	public String getDescription() {
		String text;
		text = this.getName() + " : ";
		if (!this.ifCards.isEmpty())
			for (IfCard ifCard : ifCards.values())
				text += "\n- " + ifCard.getDescription();
		return text;
	}

	public synchronized NodeAgent getAgent() throws AddressException {
		if (this.agent == null)
			this.agent = new NodeAgent(this);
		return this.agent;
	}
}
