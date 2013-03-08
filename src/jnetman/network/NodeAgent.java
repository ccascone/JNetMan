package jnetman.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jnetman.session.SessionPref;
import jnetman.snmp.MIB;
import jnetman.snmp.SnmpErrorException;
import jnetman.snmp.SnmpSyntaxException;
import jnetman.snmp.Table;
import jnetman.snmp.TimeoutException;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;

public class NodeAgent extends Agent {

	private Node node;

	public NodeAgent(Node node) throws AddressException {
		super(node);
		this.node = node;
	}

	/**
	 * Update the list of InterfaceCards declared for the node based on the data
	 * retrieved from SNMP.
	 * 
	 * @param ifDescrFilter
	 *            Update or add only interface cards whose name start with this
	 *            filter (e.g. 'eth' to update or add eth1, eth2, etc.). 'Null'
	 *            to not filter.
	 * @param ifTypeFilter
	 *            update or add only interface cards with this MIB-II ifType
	 *            value (e.g '6' to update or add only ethernet interfaces).
	 *            '-1' to not filter. For a list of other possible ifType values
	 *            refer to RFC1213-MIB.
	 * @return True if changes are made, false elsewhere
	 */
	public boolean updateIfCards(String ifDescrFilter, int ifTypeFilter) {

		logger.debug("Update of interface cards started...");

		boolean changes = false;

		Table ifTable = snmpHelper.getTable(MIB.IfTable);
		Table ipAddrTable = snmpHelper.getTable(MIB.IpAddrTable);

		IfCard ifCard = null;
		String name;
		int ifType;
		int ifIndex;
		InetAddress address;
		short prefixLength;
		for (Table.Row ifRow : ifTable.getMap().values()) {
			name = ifRow.getVariable(MIB.IfDescr).toString();
			ifType = ifRow.getVariable(MIB.IfType).toInt();
			ifIndex = ifRow.getVariable(MIB.IfIndex).toInt();

			logger.debug("Interface found >> name = " + name + ", ifType = "
					+ ifType + ", ifIndex = " + ifIndex);

			// filter and create or get
			if (ifDescrFilter == null || name.startsWith(ifDescrFilter))
				if (ifTypeFilter == -1 || ifType == ifTypeFilter)
					try {
						ifCard = node.createInterfaceCard(name);
						logger.debug("New interface created >> " + name);
						changes = true;
					} catch (DuplicateElementException e) {
						logger.debug("Interface already existing  >> " + name);
						ifCard = node.getInterfaceCard(name);
					}
				else
					continue;
			else
				continue;

			try {
				ifCard.getAgent().ifIndex = ifIndex;
			} catch (AddressException e) {
				// we don't care
			}

			// now we look at the ipaddrTable to find the ip address of this
			// interface
			for (Table.Row ipRow : ipAddrTable.getMap().values())
				if (ipRow.getVariable(MIB.IpAdEntIfIndex).toInt() == ifIndex) {
					try {
						address = InetAddress.getByName(ipRow.getVariable(
								MIB.IpAdEntAddr).toString());
						prefixLength = InetAddressUtils.toPrefixLenght(ipRow
								.getVariable(MIB.IpAdEntNetMask).toString());
						logger.debug("IP address found for interface " + name
								+ " >> " + address.getHostAddress() + "/"
								+ prefixLength);
						if (ifCard.getAddress() == null
								|| !ifCard.getAddress().equals(address)) {
							ifCard.setAddress(address);
							logger.debug("IP address updated for interface "
									+ name + " >> "
									+ ifCard.getAddress().getHostAddress());
							changes = true;
						}
						if (ifCard.getAddrPrefixLenght() != prefixLength) {
							ifCard.setAddrPrefixLenght(prefixLength);
							logger.debug("Network prefix length updated for interface "
									+ name
									+ " >> "
									+ ifCard.getAddrPrefixLenght());
							changes = true;
						}
					} catch (UnknownHostException e) {
						// no way to be here because we setted address in ip
						// format, not host name format
						e.printStackTrace();
					}
				}
		}

		return changes;
	}

	public long getIpOutNoRoutes() throws TimeoutException, SnmpErrorException,
			SnmpSyntaxException {
		return snmpHelper.getLong(MIB.IpOutNoRoutes);
	}

	public Map<IfCard, Integer> getOspfCosts(int tos) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		return getOspfCosts(new HashSet<IfCard>(node.getIfCards()), tos);
	}

	public Map<IfCard, Integer> getOspfCosts(Set<IfCard> ifCards, int tos)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		// FIXME probably buggy, we are assuming that the ip address for the
		// interface is always declared, so we don't need to use the
		// ospfIfMetricAddressLessIf index. Check the indexes of
		// OSPF-MIB::ospfIfMetricTable for further informations
		IfCard[] ifCardsArr = new IfCard[ifCards.size()];
		OID[] oids = new OID[ifCards.size()];

		int i = 0;
		for (IfCard ifCard : ifCards) {
			ifCardsArr[i] = ifCard;
			oids[i] = MIB.getOspfIfMetricEntryOID(MIB.ospfIfMetricValue,
					ifCard.getAddress(), 0, tos);
			i++;
		}
		int[] metrics = snmpHelper.getInt(oids);

		HashMap<IfCard, Integer> costs = new HashMap<IfCard, Integer>();
		for (i = 0; i < ifCardsArr.length; i++) {
			costs.put(ifCardsArr[i], metrics[i]);
		}

		return costs;
	}

	public Map<IfCard, Boolean> setOspfCosts(Map<IfCard, Integer> map, int tos)
			throws TimeoutException, SnmpErrorException, SnmpSyntaxException {
		// FIXME probably buggy, we are assuming that the ip address for the
		// interface is always declared, so we don't need to use the
		// ospfIfMetricAddressLessIf index. Check the indexes of
		// OSPF-MIB::ospfIfMetricTable for further informations
		IfCard[] ifCardsArr = new IfCard[map.size()];
		OID[] oids = new OID[map.size()];
		int[] values = new int[map.size()];
		int[] smiSyntaxs = new int[map.size()];

		int i = 0;
		for (IfCard ifCard : map.keySet()) {
			ifCardsArr[i] = ifCard;
			oids[i] = MIB.getOspfIfMetricEntryOID(MIB.ospfIfMetricValue,
					ifCard.getAddress(), 0, tos);
			values[i] = map.get(ifCard);
			i++;
		}
		Arrays.fill(smiSyntaxs, SMIConstants.SYNTAX_INTEGER32);
		HashMap<IfCard, Boolean> resMap = new HashMap<IfCard, Boolean>();

		if (SessionPref.isCumulativeSetRequestsAllowed()) {
			boolean[] res = snmpHelper.setInt(oids, values, smiSyntaxs);
			for (i = 0; i < ifCardsArr.length; i++) {
				resMap.put(ifCardsArr[i], res[i]);
			}
		} else {
			for (i = 0; i < ifCardsArr.length; i++) {
				resMap.put(ifCardsArr[i], snmpHelper.setInt(oids[i], values[i],
						SMIConstants.SYNTAX_INTEGER32));
				if (SessionPref.getOspfCostSetRequestMillisInterval() > 0)
					try {
						Thread.sleep(SessionPref
								.getOspfCostSetRequestMillisInterval());
					} catch (InterruptedException e) {
					}
			}
		}

		return resMap;
	}
}
