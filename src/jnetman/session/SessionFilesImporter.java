package jnetman.session;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jnetman.network.DuplicateElementException;
import jnetman.network.IfCard;
import jnetman.network.Link;
import jnetman.network.LinkAlreadyConnectedException;
import jnetman.network.Network;
import jnetman.network.Node;
import jnetman.session.Session;
import jnetman.session.PropertiesParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SessionFilesImporter {

	static Logger logger = Logger.getLogger("utils.sessionFilesImporter");
	static private Network network;

	/**
	 * Create a network based on the informations contained in nodes file and
	 * links file contained in the session folder
	 * 
	 * @return a Network object with nodes and links as described by session
	 *         files, null if an error occurred
	 */
	static public Network getNetwork() {
		network = new Network();

		parseNodesFile();
		parseLinksFile();

		return network;
	}

	private static void parseNodesFile() {
		PropertiesParser prop = new PropertiesParser(
				Session.getSessNodesFile());

		String[] keys;
		String nodeName;
		String ifCardName;
		Node node;
		IfCard ifCard;
		String[] addr = null;
		for (String key : prop.keySet()) {
			// key = nodeName.ifCardName
			keys = StringUtils.split(key, ".");
			if (keys.length > 2) {
				prop.fatalError(key, "illegal property key");
			}

			nodeName = keys[0];

			// creates or retrieves the node
			try {
				node = network.createNode(nodeName);
			} catch (DuplicateElementException e) {
				// already created by a previous key
				node = network.getNode(nodeName);
			}
			
			if(keys.length == 1){
				try {
					node.setAddress(InetAddress.getByName(prop.getString(key)));
					continue;
				} catch (UnknownHostException e) {
					prop.fatalError(key, "unable to parse input '" + prop.getString(key)
							+ "' as an IP address");
				}
			}
			
			ifCardName = keys[1];

			// creates or retrieves the ifCard
			try {
				ifCard = node.createInterfaceCard(ifCardName);
				// parses the CIDR address (e.g. 192.168.7.1/24)
				addr = StringUtils.split(prop.getString(key), "/");
				if (addr.length != 2)
					prop.fatalError(key,
							"unable to parse input '" + prop.getString(key)
									+ "' ad a CIDR address");
				ifCard.setAddress(InetAddress.getByName(addr[0]));
				ifCard.setAddrPrefixLenght(Short.parseShort(addr[1]));
			} catch (DuplicateElementException e) {
				prop.fatalError(key, "duplicate entry");
			} catch (UnknownHostException e) {
				prop.fatalError(key, "unable to parse input '" + addr[0]
						+ "' as an IP address");
			} catch (NumberFormatException e) {
				prop.fatalError(key, "unable to parse input '" + addr[1]
						+ "' as a port number");
			}
		}
	}

	private static void parseLinksFile() {
		PropertiesParser prop = new PropertiesParser(
				Session.getSessLinksFile());

		for (String key : prop.keySet()) {
			// key = linkName|| linkName.nominalSpeed
			String[] keys = StringUtils.split(key, '.');
			String linkName = keys[0];

			if (keys.length == 1) {
				// key = linkName
				if (network.getLink(linkName) == null)
					parseLink(key, prop);

			} else if (keys.length == 2
					&& keys[1].equalsIgnoreCase("nominalSpeed")) {

				Link link = network.getLink(linkName);
				if (link == null)
					link = parseLink(linkName, prop);

				link.setNominalSpeed(prop.getLong(key));

			} else {
				prop.fatalError(key, "illegale property key");
			}

		}
	}

	private static Link parseLink(String linkName, PropertiesParser prop) {
		Link link = null;
		try {
			link = network.createLink(linkName);
		} catch (DuplicateElementException e) {
			prop.fatalError(linkName, e.getMessage());
		}

		String[] ifCardNames = prop.getStringArray(linkName);

		if (ifCardNames.length < 1 || ifCardNames.length > 3) {
			prop.fatalError(linkName, "links can only be declared using "
					+ "the hierarchical name of at least 1 or maximum 2 "
					+ "network interfaces separated by a space");
		}

		for (String ifCardName : ifCardNames) {
			// ifCard lookup by hierarchical name e.g. 'r1.eth0'
			IfCard ifCard = network.getIfCard(ifCardName);
			if (ifCard == null) {
				prop.fatalError(linkName, "no such ifCard '" + ifCardName + "'");
			}
			try {
				link.setEndpoint(ifCard);
			} catch (LinkAlreadyConnectedException e) {
				// BUG if here.
				prop.fatalError(linkName, e.getMessage());
			}
		}

		return link;
	}

}
