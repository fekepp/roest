package net.fekepp.roest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.ros.address.InetAddressFactory;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.node.client.MasterClient;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.semanticweb.yars.nx.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;

import net.fekepp.roest.ros.NodeMessageReflectionMapper;

public class ControllerImplementation extends AbstractController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private URI masterUri;
	private NodeConfiguration nodeConfiguration;
	private NodeMainExecutor nodeMainExecutor;
	private MasterClient masterClient;

	private Cache<String, Set<Node[]>> messageCache;
	private Cache<String, String> messageTypeCache;
	private Cache<String, Cache<String, Set<Node[]>>> messageQueueCache;

	public ControllerImplementation() {

		// default URI of the ROS master
		masterUri = NodeConfiguration.DEFAULT_MASTER_URI;

		// getting URI of the ROS master from the environment
		try {
			String rosMasterUriStringFromEnvironmentVariable = System.getenv("ROS_MASTER");

			// null if environment variable is not set
			if (rosMasterUriStringFromEnvironmentVariable != null) {
				masterUri = new URI(rosMasterUriStringFromEnvironmentVariable);
				log.debug("overriding ros master URI with environment variable ROS_MASTER {}", masterUri);
			}
		} catch (SecurityException | URISyntaxException e) {
			log.error("caught the following exception when considering the ROS_MASTER environment variable:", e);
		}

		// the config.xml overrides
		if (Configuration.getRosMasterUri() != null)
		try {
			masterUri = new URI(Configuration.getRosMasterUri());
			log.debug("overriding ros master URI with config.xml field rosMasterUri {}", masterUri);
		} catch (URISyntaxException e) {
			log.error("Wrong ROS master URI syntax in config.xml", e);
		}

		log.info("ros master URI in use: {}", masterUri);

		nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		masterClient = new MasterClient(masterUri);

		messageCache = Caffeine.newBuilder().maximumSize(10000).build();
		messageTypeCache = Caffeine.newBuilder().maximumSize(10000).build();
		messageQueueCache = Caffeine.newBuilder().maximumSize(10000).build();

	}

	@Override
	protected void startup() {

		nodeConfiguration = buildNodeConfiguration(masterUri);
		
		NodeMessageReflectionMapper standardMessageNode = new NodeMessageReflectionMapper();
		standardMessageNode.setMasterClient(masterClient);
		standardMessageNode.setMessageCache(messageCache);
		standardMessageNode.setMessageTypeCache(messageTypeCache);
		standardMessageNode.setMessageQueueCache(messageQueueCache);
		nodeMainExecutor.execute(standardMessageNode, nodeConfiguration);

		while (!standardMessageNode.isInitialized()) {
			try {
//				log.info("Waiting!");
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void shutdown() {

		nodeMainExecutor.shutdown();

	}

	private NodeConfiguration buildNodeConfiguration(URI masterUri) {

		// Determining the IP where to respond to the ROS RPC calls:
		// default:
		String host = InetAddressFactory.newLoopback().getHostAddress();

		// from environment variable ROS_HOSTNAME
		String rosHoststringFromEnvironmentVariable = System.getenv("ROS_HOSTNAME");
		// null if not set
		if (rosHoststringFromEnvironmentVariable != null)
			try {
				host = InetAddressFactory.newFromHostString(rosHoststringFromEnvironmentVariable).getHostAddress();
				log.debug("overriding ros ip with environment variable ROS_HOSTNAME {}", host);
			} catch (RosRuntimeException e) {
				log.warn("Could not parse environment variable ROS_HOSTNAME due to ", e);
			}

		// from environment variable ROS_IP
		String rosIPstringFromEnvironmentVariable = System.getenv("ROS_IP");
		// null if environment variable is not set
		if (rosIPstringFromEnvironmentVariable != null)
			try {
				host = InetAddressFactory.newFromHostString(rosIPstringFromEnvironmentVariable).getHostAddress();
				log.debug("overriding ros ip with environment variable ROS_IP {}", host);
			} catch (RosRuntimeException e) {
				log.warn("Could not parse environment variable ROS_IP due to ", e);
			}
		
		// from config.xml field rosHostname
		String rosHostnameFromConfigXml = Configuration.getRosHostname();
		if (rosHostnameFromConfigXml != null)
			try {
				host = InetAddressFactory.newFromHostString(rosHostnameFromConfigXml).getHostAddress();
				log.debug("overriding ros ip with config.xml field rosHostname {}", host);
			} catch (RosRuntimeException e) {
				log.warn("Could not parse config.xml field rosHostname due to ", e);
			}
		

		String rosIpFromConfigXML = Configuration.getRosIp();
		if (rosHostnameFromConfigXml != null)
			try {
				host = InetAddressFactory.newFromHostString(rosIpFromConfigXML).getHostAddress();
				log.debug("overriding ros ip with config.xml field rosIp {}", host);
			} catch (RosRuntimeException e) {
				log.warn("Could not parse config.xml field rosIp due to ", e);
			}
		
		log.info("ros IP in use: {}", host);

		NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(host);

		GraphName namespace = GraphName.root();
		Map<GraphName, GraphName> remappings = Maps.newHashMap();
		NameResolver nameResolver = new NameResolver(namespace, remappings);
		nodeConfiguration.setParentResolver(nameResolver);
		
		nodeConfiguration.setMasterUri(masterUri);
		
		return nodeConfiguration;

	}

	public URI getMasterUri() {
		return masterUri;
	}

	public void setMasterUri(URI masterUri) {
		this.masterUri = masterUri;
	}

	public Cache<String, Set<Node[]>> getMessageCache() {
		return messageCache;
	}

	public Cache<String, String> getMessageTypeCache() {
		return messageTypeCache;
	}

	public Cache<String, Cache<String, Set<Node[]>>> getMessageQueueCache() {
		return messageQueueCache;
	}

}