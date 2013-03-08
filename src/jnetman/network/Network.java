package jnetman.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is the main class of the Network Manager. It provides an high level
 * abstraction of a network, defined as its collections of nodes and link. It
 * provides methods for creating and retrieving nodes and links, as well as
 * helper methods to easily manage the network.
 * 
 * @author Carmelo Cascone
 * 
 */
public class Network {

	private Map<String, Node> nodes;
	private Map<String, Link> links;
	protected static Logger logger = Logger.getLogger("network");

	/**
	 * Creates a new Network.
	 */
	public Network() {
		this.nodes = new HashMap<String, Node>();
		this.links = new HashMap<String, Link>();
		logger.debug("New network created");
	}

	/**
	 * Adds a link to the links collection. This method is protected and so
	 * should be used only inside the NetworkManager package. See also
	 * createLink.
	 * 
	 * @param link
	 *            Link to be added to the network
	 * @throws DuplicateElementException
	 *             if a link with the same name already exist in the network
	 */
	private void addLink(Link link) throws DuplicateElementException {
		if (this.links.containsKey(link.getName()))
			throw new DuplicateElementException("A link named '"
					+ link.getName() + "' already exist");
		this.links.put(link.getName(), link);
		logger.debug("New link added >> " + link.getName());
	}

	/**
	 * Get a node from the nodes collection
	 * 
	 * @param name
	 *            Name of the node to be returned
	 * @return Node if a node with this name exists in the network, else null
	 */
	public Node getNode(String name) {
		return this.nodes.get(name);
	}

	/**
	 * Adds a node to the nodes collection. This method is protected and so
	 * should be used only inside the NetworkManager package. See also
	 * createNode.
	 * 
	 * @param node
	 *            Node to be added to the network
	 * @throws DuplicateElementException
	 *             If a node with the same name already exist in the network
	 */
	protected void addNode(Node node) throws DuplicateElementException {
		if (this.nodes.containsKey(node.getName()))
			throw new DuplicateElementException("A node named '"
					+ node.getName() + "' already exist");
		this.nodes.put(node.getName(), node);
		logger.debug("New node added >> " + node.getName());
	}

	/**
	 * Create a new node and adds it to the network.
	 * 
	 * @param name
	 *            Name of the node to be created
	 * @return Node created
	 * @throws DuplicateElementException
	 *             If a node with the same name already exists in the network
	 */
	public Node createNode(String name) throws DuplicateElementException {
		if (this.nodes.containsKey(name))
			throw new DuplicateElementException("A node named '" + name
					+ "' already exist");

		Node newNode = new Node(name, this);
		this.addNode(newNode);
		return newNode;

	}

	/**
	 * Get a link from the links collection
	 * 
	 * @param name
	 *            Name of the link to be returned
	 * @return Link if a link with this name exists in the network, else null
	 */
	public Link getLink(String name) {
		return this.links.get(name);
	}

	/**
	 * Create a new link and adds it to the network. This is the only method
	 * that can be used from other external packages to create a link.
	 * 
	 * @param name
	 *            Name of the link to be created
	 * @return Link created
	 * @throws DuplicateElementException
	 *             If a link with the same name already exists in the network
	 */
	public Link createLink(String name) throws DuplicateElementException {
		if (this.links.containsKey(name))
			throw new DuplicateElementException("A link named '" + name
					+ "' already exist");

		Link newLink = new Link(name, this);
		this.addLink(newLink);
		return newLink;
	}

	/**
	 * Connect an IfCard to a link selected by it's name. This is an helper
	 * method to easily create and connect a link. Another way is to first
	 * create a link and then connect it to an IfCard using its own method.
	 * 
	 * @param ifCard
	 *            IfCard to connect
	 * @param linkName
	 *            Name of the link to connect to the interface
	 * @throws LinkAlreadyConnectedException
	 *             If the requested link is already connected both sides.
	 */
	public Link assignIfCardToLink(IfCard ifCard, String linkName)
			throws LinkAlreadyConnectedException {
		Link link = null;
		try {
			link = this.createLink(linkName);
		} catch (DuplicateElementException e1) {
			link = links.get(linkName);
		}
		link.setEndpoint(ifCard);
		return link;
	}

	/**
	 * To obtain a collection of network nodes
	 * 
	 * @return Collection of nodes in the network
	 */
	public Collection<Node> getNodes() {
		return this.nodes.values();
	}

	/**
	 * Get a ifCard from the network from a hierarchical name (e.g, node1.eth0)
	 * 
	 * @param Hierarchical
	 *            name of the interface
	 * @return IfCard if a ifCard with this hierarchical name exisits in the
	 *         network, null elsewhere
	 */
	public IfCard getIfCard(String name) {
		String[] s = StringUtils.split(name, '.');
		if (s.length != 2)
			return null;

		Node node = this.getNode(s[0]);
		if (node == null)
			return null;

		return node.getInterfaceCard(s[1]);
	}
	
	/**
	 * Get a ifCard from the network from a hierarchical name (e.g, node1.eth0)
	 * 
	 * @param Hierarchical
	 *            name of the interface
	 * @return IfCard if a ifCard with this hierarchical name exisits in the
	 *         network, null elsewhere
	 */
	public IfCard getIfCard(Node node, Link link) {
		for(IfCard ifCard : node.getIfCards()){
			if(ifCard.hasLink() && ifCard.getLink() == link)
				return ifCard;
		}
		return null;
	}

	/**
	 * To obtain a collection of network links
	 * 
	 * @return Collection of links in the network
	 */
	public Collection<Link> getLinks() {
		return this.links.values();
	}

}
