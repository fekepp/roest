package net.fekepp.roest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.ros.address.InetAddressFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import net.fekepp.roest.ros.StandardMessageNode;

public class ControllerImplementation extends ControllerAbstract {

	private final static Configuration CONFIGURATION = Configuration.getInstance();

	private final Logger log = LoggerFactory.getLogger(getClass());

	private NodeConfiguration nodeConfiguration;
	private URI masterUri;
	private NodeMainExecutor nodeMainExecutor;
	private MasterClient masterClient;
	// private MasterStateClient masterStateClient;
	// private MessageInterfaceClassProvider messageInterfaceClassProvider;

	private ConcurrentMap<String, String> messageCache;
	private ConcurrentMap<String, String> messageTypeCache;

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

		messageCache = new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(1000).build();
		messageTypeCache = new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(1000).build();

	}

	@Override
	protected void startup() {

		nodeConfiguration = buildNodeConfiguration(masterUri);

		// NodeMain nodeMain = new TestNode();
		// nodeMainExecutor.execute(nodeMain, nodeConfiguration);

		StandardMessageNode standardMessageNode = new StandardMessageNode();
		standardMessageNode.setMasterClient(masterClient);
		standardMessageNode.setMessageCache(messageCache);
		standardMessageNode.setMessageTypeCache(messageTypeCache);
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

	public ConcurrentMap<String, String> getMessageCache() {
		return messageCache;
	}

	public ConcurrentMap<String, String> getMessageTypeCache() {
		return messageTypeCache;
	}

}