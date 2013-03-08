package jnetman.session;

import java.io.File;
import java.io.FileNotFoundException;

import jnetman.Constants;
import jnetman.network.Network;

import org.apache.log4j.Logger;

public class Session {

	static private boolean loaded = false;
	static private File sessionRootDir;
	static private Network network;
	static private Logger logger = Logger.getLogger("session");

	static public void loadSession(String pathToSessionRootDir)
			throws FileNotFoundException {
		if (loaded)
			throw new UnsupportedOperationException("Session already loaded");

		sessionRootDir = new File(pathToSessionRootDir);
		if (!sessionRootDir.exists() || !sessionRootDir.isDirectory())
			throw new FileNotFoundException(
					"The path to the session directory passed doesn't exist or is not a directory");
		loaded = true;
	}

	static public void loadDummySession() throws FileNotFoundException {
		String path = ClassLoader.getSystemClassLoader()
				.getResource("dummy_session").getPath();
		File pathFile = new File(path);
		loadSession(pathFile.getAbsolutePath());
	}

	static public File getSessionDir() {
		if (!loaded)
			throw new UnsupportedOperationException("Session not loaded");
		return sessionRootDir;
	}

	static public File getSessPropertiesFile() {
		return getChildFileRequired(getSessionDir(),
				Constants.SESS_PROPERTIES_FILENAME);
	}

	static public File getLogPropertiesFile() throws FileNotFoundException {
		return getChildFile(getSessionDir(), Constants.LOG_PROPERTIES_FILENAME);
	}

	static public File getSessAutodiscoveryFile() throws FileNotFoundException {
		return getChildFile(getSessionDir(),
				Constants.SESS_AUTODISCOVERY_FILENAME);
	}

	static public File getSessNodesFile() {
		return getChildFileRequired(getSessionDir(), Constants.SESS_NODES_FILENAME);
	}

	static public File getSessLinksFile() {
		return getChildFileRequired(getSessionDir(), Constants.SESS_LINKS_FILENAME);
	}

	static public File getSnmpPropertiesFile() {
		return getChildFileRequired(getSessionDir(),
				Constants.SNMP_PROPERTIES_FILENAME);
	}

	public static File getChildFile(File parentDir, String filename)
			throws FileNotFoundException {
		File file = new File(parentDir.getAbsolutePath() + File.separatorChar
				+ filename);
		if (!file.exists() || !file.isFile())
			throw new FileNotFoundException("No such file "
					+ file.getAbsolutePath());
		return file;
	}

	static public File getChildFileRequired(File parentDir, String filename) {
		try {
			return getChildFile(parentDir, filename);
		} catch (FileNotFoundException e) {
			logger.fatal(e.getMessage());
			System.exit(-1);
		}
		return null;
	}

	static public File getChildDir(File parentDir, String dirname)
			throws FileNotFoundException {
		File dir = new File(parentDir.getAbsolutePath() + File.separatorChar
				+ dirname);
		if (!dir.exists() || !dir.isDirectory())
			throw new FileNotFoundException("No such directory "
					+ dir.getAbsolutePath());
		return dir;
	}

	static public boolean isLoaded() {
		return loaded;
	}

	static public void setNetwork(Network network) {
		if (Session.network != null)
			throw new UnsupportedOperationException("Network already setted");
		Session.network = network;
	}

	static public Network getNetwork() {
		if (Session.network == null)
			throw new UnsupportedOperationException("Network not setted");
		return Session.network;
	}
}
