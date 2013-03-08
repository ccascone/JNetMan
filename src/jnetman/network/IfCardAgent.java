package jnetman.network;

import java.util.HashMap;
import java.util.HashSet;

import jnetman.snmp.MIB;
import jnetman.snmp.MibHelper;
import jnetman.snmp.SnmpClient;
import jnetman.snmp.SnmpErrorException;
import jnetman.snmp.SnmpHelper;
import jnetman.snmp.SnmpSyntaxException;
import jnetman.snmp.TimedValues;
import jnetman.snmp.TimeoutException;

import org.apache.log4j.Logger;
import org.snmp4j.smi.OID;

public class IfCardAgent {

	public static final int BW_IN = 0;
	public static final int BW_OUT = 1;
	public static final int BW_MAX_IN_OUT = 2;

	private IfCard ifCard;
	private NodeAgent agent;
	protected MibHelper mibHelper;
	protected SnmpHelper snmpHelper;
	protected SnmpClient snmpClient;
	protected Logger logger;

	// MIB-2 data
	protected int ifIndex = -1;

	public IfCardAgent(IfCard ifCard) throws AddressException {
		this.ifCard = ifCard;
		agent = ifCard.getNode().getAgent();
		this.mibHelper = this.agent.getMibHelper();
		this.snmpHelper = this.agent.getSnmpHelper();
		this.snmpClient = this.agent.getSnmpClient();
		logger = Logger.getLogger("network.ifCardAgent."
				+ ifCard.getHierarchicalName());
		logger.debug("New interface agent created");
	}

	public int getIfIndex() throws TimeoutException, IfCardException,
			SnmpErrorException {
		if (this.ifIndex == -1) {
			logger.debug("IfIndex is missing, trying to obtain the value from MIB-II...");
			ifIndex = mibHelper.lookupIfIndex(ifCard.getName());
			/*
			 * If it's again -1 means that something went wrong
			 */
			if (this.ifIndex == -1)
				throw new IfCardException(
						"Unable to find ifIndex for interface "
								+ ifCard.getHierarchicalName());

			logger.debug("New ifIndex value discovered >> " + ifIndex);
		}
		return this.ifIndex;
	}

	public long getCurrentBitrate(int seconds, int direction)
			throws TimeoutException, SnmpErrorException, IfCardException, SnmpSyntaxException {

		logger.debug("Current average speed evaluation started...");

		OID[] ifInOutOctetsOids = new OID[] {
				new OID(MIB.IfInOctets).append(this.getIfIndex()),
				new OID(MIB.IfOutOctets).append(this.getIfIndex()) };

		TimedValues initialValues = snmpHelper
				.getAsTimedValues(ifInOutOctetsOids);
		long[] initialOctets = SnmpHelper.castToLong(initialValues.getValues());

		logger.debug("Initial values received >> [inOctets = "
				+ initialOctets[0] + " byte, outOctets = " + initialOctets[1]
				+ " byte, millis = " + initialValues.getMillis() + " ms]");

		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			logger.debug("Sleep interrupted!");
		}

		TimedValues finalValues = snmpHelper
				.getAsTimedValues(ifInOutOctetsOids);
		long[] finalOctets = SnmpHelper.castToLong(finalValues.getValues());

		logger.debug("Final values received >> [inOctets = " + finalOctets[0]
				+ " byte, outOctets = " + finalOctets[1] + " byte, millis = "
				+ finalValues.getMillis() + " ms]");

		/*
		 * Compute formula.
		 */
		long deltaInBits = (finalOctets[0] - initialOctets[0]) * 8;
		long deltaOutBits = (finalOctets[1] - initialOctets[1]) * 8;
		float deltaSeconds = initialValues.getMillisDiff(finalValues) / 1000;

		logger.debug("Evaluating average speed >> [deltaInBits = "
				+ deltaInBits + " bit, deltaOutBits = " + deltaOutBits
				+ " bit, deltaSeconds = " + deltaSeconds + " s]");

		float bandwith;
		if (direction == IfCardAgent.BW_OUT
				|| (direction == IfCardAgent.BW_MAX_IN_OUT && deltaOutBits > deltaInBits)) {
			bandwith = deltaOutBits / deltaSeconds;
			logger.debug("Average output speed  >> " + bandwith + " bit/s");
		} else {
			bandwith = deltaInBits / deltaSeconds;
			logger.debug("Average input speed >> " + bandwith + " bit/s");
		}

		long longBandwith = Math.round(bandwith);

		ifCard.updatePeakSpeed(longBandwith);

		return longBandwith;

	}

	public int getOspfCost(int tos) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		HashSet<IfCard> set = new HashSet<IfCard>();
		set.add(ifCard);
		return agent.getOspfCosts(set, tos).get(this);
	}

	public boolean setOspfCost(int tos, int cost) throws TimeoutException,
			SnmpErrorException, SnmpSyntaxException {
		HashMap<IfCard, Integer> map = new HashMap<IfCard, Integer>();
		map.put(ifCard, cost);
		return agent.setOspfCosts(map, tos).get(this);
	}

}
