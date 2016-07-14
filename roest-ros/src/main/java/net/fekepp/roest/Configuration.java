package net.fekepp.roest;

import java.io.File;
import java.io.IOException;

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

	private static final String CONFIG_KEY_HOST = "host";

	private static final String CONFIG_KEY_PORT = "port";

	private static final String CONFIG_KEY_ROS_MASTER_URI = "rosMasterUri";

	private static final String CONFIG_KEY_ROS_HOSTNAME = "rosHostname";

	private static final String CONFIG_KEY_ROS_IP = "rosIp";

	private static final String CONFIG_KEY_QUEUE_EXPIRATION_TIME = "queueExpirationTime";

	private static final String CONFIG_KEY_QUEUE_MAXIMAL_SIZE = "queueMaximalSize";

	private static final String CONFIG_KEY_TOPIC_NAMES = "topicNames.topicName";

	public static String getAppDirectoryPath() {

		String packageName = Configuration.class.getPackage().getName();
		String packageNameShort = packageName.substring(packageName.lastIndexOf(".") + 1, packageName.length());

		// Default to a unix style hidden folder in the user home directory
		String configurationFilePath = System.getProperty("user.home") + "/." + packageNameShort;

		if (OperatingSystemDetector.isMacOsX()) {

			// See http://developer.apple.com/library/mac/#qa/qa1170/_index.html
			configurationFilePath = System.getProperty("user.home") + "/Library/Preferences/" + packageName;
		}

		if (OperatingSystemDetector.isWindows()) {
			configurationFilePath = System.getenv("LOCALAPPDATA") + "\\" + packageNameShort;
		}

		return configurationFilePath;

	}

	public static Configuration getInstance() {
		return Holder.INSTANCE;
	}

	public static String getHost() {
		return Holder.INSTANCE.getString(CONFIG_KEY_HOST);
	}

	public static int getPort() {
		return Holder.INSTANCE.getInt(CONFIG_KEY_PORT);
	}

	public static String getRosMasterUri() {
		return Holder.INSTANCE.getString(CONFIG_KEY_ROS_MASTER_URI);
	}

	public static String getRosHostname() {
		return Holder.INSTANCE.getString(CONFIG_KEY_ROS_HOSTNAME);
	}

	public static String getRosIp() {
		return Holder.INSTANCE.getString(CONFIG_KEY_ROS_IP);
	}

	public static int getQueueExpirationTime() {
		return Holder.INSTANCE.getInt(CONFIG_KEY_QUEUE_EXPIRATION_TIME);
	}

	public static int getQueueMaximalSize() {
		return Holder.INSTANCE.getInt(CONFIG_KEY_QUEUE_MAXIMAL_SIZE);
	}

	public static String[] getTopicNames() {
		return Holder.INSTANCE.getStringArray(CONFIG_KEY_TOPIC_NAMES);
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
			log.info("Default configuration loaded > {}", configurationDefault.getFile());
		} catch (ConfigurationException e) {
			log.warn("Default configuration could not be located and loaded");
		}

		try {
			configurationDistribution.load(CONFIG_FILE_NAME_DISTRIBUTION);
			log.info("Loaded distribution configuration > {}", configurationDistribution.getFile());
		} catch (ConfigurationException e) {
			log.info("Distribution configuration could not be located and loaded");
		}

		File configurationUserFile = new File(new File(getAppDirectoryPath()), CONFIG_FILE_NAME_USER);

		if (!configurationUserFile.exists()) {

			try {
				FileUtils.touch(configurationUserFile);
				FileUtils.writeStringToFile(configurationUserFile, CONFIG_EMTPY, "UTF-8");
			} catch (IOException e) {
				log.error("User configuration file creation failed > {}", configurationUserFile, e);
			}

			log.info("User configuration file created > {}", configurationUserFile);

		}

		try {
			configurationUser.load(configurationUserFile);
			configurationUser.setFileName(CONFIG_FILE_NAME_USER);
			log.info("User configuration loaded > {}", configurationUser.getFile());
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
