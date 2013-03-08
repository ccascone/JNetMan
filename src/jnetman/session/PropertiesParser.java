package jnetman.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class PropertiesParser {

	private File file;
	private Properties prop;
	static private Logger logger = Logger.getLogger("utils.propertiesParser");

	public PropertiesParser(File file) {
		this.file = file;
		prop = new Properties();
		try {
			FileInputStream is = new FileInputStream(file);
			prop.load(is);
			is.close();
		} catch (IOException e) {
			fatalError(e.getMessage());
		}
	}

	public void fatalError(String s) {
		logger.fatal("[" + file.getAbsolutePath() + "] " + s);
		System.exit(-1);
	}

	public void fatalError(String key, String s) {
		fatalError("In property '" + key + "': " + s);
	}

	public boolean hasProperty(String key) {
		if (prop.getProperty(key) == null)
			return false;
		else
			return true;
	}

	private String getProperty(String key) {
		String val = prop.getProperty(key);
		if (val == null) {
			fatalError("Property '" + key + "' not found");
			System.exit(-1);
		}
		return val;
	}

	public String getString(String key) {
		return StringUtils.trim(getProperty(key));
	}

	public boolean getBoolean(String key) {
		String val = getString(key);
		if (val.equalsIgnoreCase("true"))
			return true;
		else if (val.equalsIgnoreCase("false"))
			return false;

		fatalError(key, "input '" + val + "' can't be parsed as a boolean");
		return false;
	}

	public int getInt(String key) {
		try {
			return Integer.valueOf(getString(key));
		} catch (NumberFormatException e) {
			fatalError(key, "input '" + getString(key)
					+ "' can't be parsed as an integer");
		}
		return 0;
	}

	public long getLong(String key) {
		try {
			return Long.valueOf(getString(key));
		} catch (NumberFormatException e) {
			fatalError(key, "input '" + getString(key)
					+ "' can't be parsed as a long");
		}
		return 0;
	}

	public float getFloat(String key) {
		try {
			return Float.valueOf(getString(key));
		} catch (NumberFormatException e) {
			fatalError(key, "input '" + getString(key)
					+ "' can't be parsed as a float");
		}
		return 0;
	}

	public Double getDouble(String key) {
		try {
			return Double.valueOf(getString(key));
		} catch (NumberFormatException e) {
			fatalError(key, "input '" + getString(key)
					+ "' can't be parsed as a double");
		}
		return (double) 0;
	}

	public String[] getStringArray(String key) {
		String val = getString(key);
		if (val == null)
			return null;
		return StringUtils.split(val);
	}

	public Set<String> keySet() {
		return keySet(null);
	}

	public Set<String> keySet(String prefix) {
		Set<String> keys = new HashSet<String>();
		for (String key : prop.keySet().toArray(new String[0])) {
			if (prefix == null || StringUtils.startsWith(key, prefix))
				keys.add(key);
		}
		return keys;

	}

	public Properties getProperties() {
		return this.prop;
	}
}
