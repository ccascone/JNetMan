package jnetman.session;


public class SessionPref {

	static private PropertiesParser prop = new PropertiesParser(
			Session.getSessPropertiesFile());

	static public int getMeasuringInterval() {
		return prop.getInt("MEASURING_INTERVAL");
	}

	static public int getOspfIfMetricTos() {
		return prop.getInt("OSPF_IF_METRIC_TOS");
	}
	
	static public boolean isCumulativeSetRequestsAllowed() {
		return prop.getBoolean("CUMULATIVE_SET_REQUESTS_ALLOWED");
	}
	
	static public long getOspfCostSetRequestMillisInterval() {
		return prop.getLong("OSPF_COST_SET_REQUEST_MILLIS_INTERVAL");
	}
	
	static public long getConvergenceTransientMillis() {
		return prop.getLong("CONVERGENCE_TRNASIENT_MILLIS");
	}
}
