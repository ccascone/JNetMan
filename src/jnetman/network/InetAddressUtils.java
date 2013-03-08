package jnetman.network;

import org.apache.commons.lang3.StringUtils;

public class InetAddressUtils {

	public static short toPrefixLenght(String netmask) {

		short prefixLenght = 0;
		short thisNum;
		String thisBinaryNum;
		for (String num : StringUtils.split(netmask, '.')) {
			thisNum = Short.parseShort(num);
			thisBinaryNum = Integer.toBinaryString(thisNum);
			prefixLenght += StringUtils.countMatches(thisBinaryNum, "1");
		}

		return prefixLenght;
	}

}
