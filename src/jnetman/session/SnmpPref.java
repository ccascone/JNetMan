package jnetman.session;


public class SnmpPref {

	static private PropertiesParser prop = new PropertiesParser(
			Session.getSnmpPropertiesFile());

	static public String getUser() {
		return prop.getString("V3_USER");
	}

	static public String getPassword() {
		return prop.getString("V3_AUTHPRIV_PASSWORD");
	}

	static public int getPort() {
		return prop.getInt("AGENT_PORT");
	}

	static public int getTrapsPort() {
		return prop.getInt("TRAPS_PORT");
	}

	static public int getTimeout() {
		return prop.getInt("TIMEOUT");
	}

	static public int getMaxRetries() {
		return prop.getInt("MAX_RETRIES");
	}

	static public boolean isSnmp4jLogEnabled() {
		return prop.getBoolean("ENABLE_SNMP4J_LOG");
	}

}
