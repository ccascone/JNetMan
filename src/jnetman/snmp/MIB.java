package jnetman.snmp;

import java.net.InetAddress;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;

public final class MIB {

	/*
	 * MIB-II Root
	 */
	public static final OID Mib2Root = new OID(".1.3.6.1.2.1");
	/*
	 * System
	 */
	public static final OID System = new OID(Mib2Root).append(1);
	public static final OID sysDescr = new OID(System).append(".1.0");
	public static final OID sysUptime = new OID(System).append(".3.0");
	/*
	 * MIB-II Interfaces
	 */
	public static final OID Interfaces = new OID(Mib2Root).append(2);
	public static final OID IfNumber = new OID(Interfaces).append(".1.0");
	public static final OID IfTable = new OID(Interfaces).append(2);
	public static final OID IfTableEntry = new OID(IfTable).append(1);
	public static final OID IfIndex = new OID(IfTableEntry).append(1);
	public static final OID IfDescr = new OID(IfTableEntry).append(2);
	public static final OID IfType = new OID(IfTableEntry).append(3);
	public static final OID IfInOctets = new OID(IfTableEntry).append(10);
	public static final OID IfOutOctets = new OID(IfTableEntry).append(16);
	/*
	 * MIB-II Ip
	 */
	public static final OID Ip = new OID(Mib2Root).append(4);
	public static final OID IpOutNoRoutes = new OID(Ip).append(".12.0");
	public static final OID IpAddrTable = new OID(Ip).append(20);
	public static final OID IpAddrEntry = new OID(IpAddrTable).append(1);
	public static final OID IpAdEntAddr = new OID(IpAddrEntry).append(1);
	public static final OID IpAdEntIfIndex = new OID(IpAddrEntry).append(2);
	public static final OID IpAdEntNetMask = new OID(IpAddrEntry).append(3);

	/*
	 * MIB-II Ip = new OID(.1.3.6.1.2.1.4
	 */
	public static final OID Ospf = new OID(Mib2Root).append(14);
	public static final OID ospfIfMetricTable = new OID(Ospf).append(8);
	public static final OID ospfIfMetricEntry = new OID(ospfIfMetricTable)
			.append(1);
	public static final OID ospfIfMetricValue = new OID(ospfIfMetricEntry)
			.append(4);

	/**
	 * Get the corresponding OID of the ospfIfMetricTable for the passed indexes
	 * 
	 * @param entryOid
	 *            ospfIfMetricEntry OID
	 * @param ipAddress
	 *            ospfIfMetricIpAddress
	 * @param addressLessIf
	 *            ospfIfMetricAddressLessIf
	 * @param ifMetricTos
	 *            ospfIfMetricTOS
	 * @return The ospfIfMetricEntry OID completed with the indexes
	 */
	public static final OID getOspfIfMetricEntryOID(OID entryOid,
			InetAddress ipAddress, int addressLessIf, int ifMetricTos) {
		IpAddress addr = new IpAddress(ipAddress);
		Integer32 lessIf = new Integer32(addressLessIf);
		Integer32 tos = new Integer32(ifMetricTos);

		return new OID(entryOid).append(addr.toSubIndex(false))
				.append(lessIf.toSubIndex(false)).append(tos.toSubIndex(false));

	}
}
