package net.fekepp.roest;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration extends CompositeConfiguration {

	private static class Holder {
		private static final Configuration INSTANCE = new Configuration();
	}

	private static final String CONFIG_FILE_NAME_DEFAULT = "config.default.xml";

	private static final String CONFIG_FILE_NAME_DISTRIBUTION = "config.xml";

	private static final String CONFIG_FILE_NAME_USER = "config.xml";

	private static final String CONFIG_EMTPY = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<configuration/>";

	public static final String CONFIG_KEY_MASTER_URI = "uri";

	public static String getAppDirectoryPath() {

		String packageName = Configuration.class.getPackage().getName();
		String packageNameShort = packageName.substring(
				packageName.lastIndexOf(".") + 1, packageName.length());

		// Default to a unix style hidden folder in the user home directory
		String configurationFilePath = System.getProperty("user.home") + "/."
				+ packageNameShort;

		if (OperatingSystemDetector.isMacOsX()) {

			// See http://developer.apple.com/library/mac/#qa/qa1170/_index.html
			configurationFilePath = System.getProperty("user.home")
					+ "/Library/Preferences/" + packageName;
		}

		if (OperatingSystemDetector.isWindows()) {
			configurationFilePath = System.getenv("LOCALAPPDATA") + "\\"
					+ packageNameShort;
		}

		return configurationFilePath;

	}

	public static Configuration getInstance() {
		return Holder.INSTANCE;
	}

	public static String getMasterUri() {
		return Holder.INSTANCE.getString(CONFIG_KEY_MASTER_URI);
	}

	public static void main(String[] args) {

		Configuration configuration = Configuration.getInstance();

		configuration.addProperty("test_key", "test_value");

		Iterator<String> keysInterator = configuration.getKeys();
		while (keysInterator.hasNext()) {
			String key = keysInterator.next();
			if (configuration.getList(key).size() == 1) {
				System.out.println(key + " = " + configuration.getString(key));
			} else {
				System.out.println(key + " = " + configuration.getList(key));
			}
		}

		configuration.clearProperty("test_key");

		try {
			configuration.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private XMLConfiguration configurationDefault;
	private XMLConfiguration configurationDistribution;
	private XMLConfiguration configurationUser;

	private Configuration() {
		// super(new XMLConfiguration());
		// configurationUser = (XMLConfiguration) getInMemoryConfiguration();

		configurationDefault = new XMLConfiguration();
		configurationDistribution = new XMLConfiguration();
		configurationUser = new XMLConfiguration();

		try {
			configurationDefault.load(CONFIG_FILE_NAME_DEFAULT);
			log.info("Default configuration loaded > {}",
					configurationDefault.getFile());
		} catch (ConfigurationException e) {
			log.warn("Default configuration could not be located and loaded");
		}

		try {
			configurationDistribution.load(CONFIG_FILE_NAME_DISTRIBUTION);
			log.info("Loaded distribution configuration > {}",
					configurationDistribution.getFile());
		} catch (ConfigurationException e) {
			log.info("Distribution configuration could not be located and loaded");
		}

		File configurationUserFile = new File(new File(getAppDirectoryPath()),
				CONFIG_FILE_NAME_USER);

		if (!configurationUserFile.exists()) {

			try {
				FileUtils.touch(configurationUserFile);
				FileUtils.writeStringToFile(configurationUserFile,
						CONFIG_EMTPY, "UTF-8");
			} catch (IOException e) {
				log.error("User configuration file creation failed > {}",
						configurationUserFile, e);
			}

			log.info("User configuration file created > {}",
					configurationUserFile);

		}

		try {
			configurationUser.load(configurationUserFile);
			configurationUser.setFileName(CONFIG_FILE_NAME_USER);
			log.info("User configuration loaded > {}",
					configurationUser.getFile());
		} catch (ConfigurationException e) {
			log.info("User configuration could not be located and loaded");
		}

		// addConfiguration(configurationUser);
		addConfiguration(configurationUser, true);
		addConfiguration(configurationDistribution);
		addConfiguration(configurationDefault);
		// addConfiguration(configurationUser, true);

	}

	public void save() throws ConfigurationException {
		configurationUser.save();
	}
}