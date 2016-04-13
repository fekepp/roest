package net.fekepp.roest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.ros.address.InetAddressFactory;
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

public class ControllerImplementation extends ControllerAbstract {

	private final static Configuration CONFIGURATION = Configuration.getInstance();

	private final Logger log = LoggerFactory.getLogger(getClass());

	private NodeConfiguration nodeConfiguration;
	private URI masterUri;
	private NodeMainExecutor nodeMainExecutor;
	private MasterClient masterClient;
	// private MasterStateClient masterStateClient;
	// private MessageInterfaceClassProvider messageInterfaceClassProvider;

	private Cache<String, Set<Node[]>> messageCache;
	private Cache<String, String> messageTypeCache;
	private Cache<String, Cache<String, Set<Node[]>>> messageQueueCache;

	public ControllerImplementation() {

		Configuration.getInstance().getString("uri");
		CONFIGURATION.getString("uri");
		Configuration.getMasterUri();
		Configuration.getInstance().getString(Configuration.CONFIG_KEY_MASTER_URI);

		masterUri = NodeConfiguration.DEFAULT_MASTER_URI;
		try {
			masterUri = new URI(Configuration.getMasterUri());
		}

		catch (URISyntaxException e) {
			log.error("Wrong ROS master URI syntax", e);
		}

		nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		masterClient = new MasterClient(masterUri);
		// messageInterfaceClassProvider = new
		// DefaultMessageInterfaceClassProvider();

		messageCache = Caffeine.newBuilder()
				// .expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(10000).build();

		messageTypeCache = Caffeine.newBuilder()
				// .expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(10000).build();

		messageQueueCache = Caffeine.newBuilder()
				// .expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(10000).build();

	}

	@Override
	protected void startup() {

		nodeConfiguration = buildNodeConfiguration(masterUri);

		// NodeMain nodeMain = new TestNode();
		// nodeMainExecutor.execute(nodeMain, nodeConfiguration);

		NodeMessageReflectionMapper standardMessageNode = new NodeMessageReflectionMapper();
		standardMessageNode.setMasterClient(masterClient);
		standardMessageNode.setMessageCache(messageCache);
		standardMessageNode.setMessageTypeCache(messageTypeCache);
		standardMessageNode.setMessageQueueCache(messageQueueCache);
		nodeMainExecutor.execute(standardMessageNode, nodeConfiguration);

		// ReflectionNode reflectionNode = new ReflectionNode();
		// reflectionNode
		// .setMessageInterfaceClassProvider(messageInterfaceClassProvider);
		// nodeMainExecutor.execute(reflectionNode, nodeConfiguration);

		while (!standardMessageNode.isInitialized()) {
			try {
				// log.info("Waiting!");
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

	private NodeConfiguration buildNodeConfiguration(URI uri) {

		String host = InetAddressFactory.newLoopback().getHostAddress();
		NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(host);

		GraphName namespace = GraphName.root();
		Map<GraphName, GraphName> remappings = Maps.newHashMap();
		NameResolver nameResolver = new NameResolver(namespace, remappings);
		nodeConfiguration.setParentResolver(nameResolver);

		nodeConfiguration.setMasterUri(uri);

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