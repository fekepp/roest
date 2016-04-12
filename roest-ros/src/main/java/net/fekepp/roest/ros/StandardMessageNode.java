package net.fekepp.roest.ros;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.master.client.TopicType;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.fekepp.roest.Configuration;
import std_msgs.MultiArrayDimension;
import std_msgs.MultiArrayLayout;

public class StandardMessageNode implements NodeMain {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Cache<String, Set<org.semanticweb.yars.nx.Node[]>> messageCache;
	private Cache<String, String> messageTypeCache;
	private Cache<String, Cache<String, Set<org.semanticweb.yars.nx.Node[]>>> messageQueueCaches;

	private MasterClient masterClient;
	private StandardMessageSerializer standardMessageSerializer = new StandardMessageSerializer();

	private boolean initialized;

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("roest/stdmsg");
	}

	public MasterClient getMasterClient() {
		return masterClient;
	}

	@Override
	public void onError(Node arg0, Throwable throwable) {
		log.info("public void onError(Node node, Throwable throwable)");
	}

	@Override
	public void onShutdown(Node node) {
		log.info("public void onShutdown(Node node)");
	}

	@Override
	public void onShutdownComplete(Node node) {
		log.info("public void onShutdownComplete(Node node)");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		Response<List<TopicType>> response = masterClient.getTopicTypes(this.getDefaultNodeName());

		String[] allowedTopicNames = Configuration.getTopicNames();
		log.info("Number of allowed topics > {}", allowedTopicNames.length);

		// StatusCode statusCode = response.getStatusCode();
		// int statusCodeInt = statusCode.toInt();
		// String statusCodeString = statusCode.toString();
		List<TopicType> result = response.getResult();

		for (TopicType topicType : result) {

			String topicName = topicType.getName();
			String topicMessageType = topicType.getMessageType();

			if (allowedTopicNames.length > 0) {
				boolean stop = true;
				for (String allowedTopicName : allowedTopicNames) {
					if (topicName.equals(allowedTopicName)) {
						stop = false;
					}
				}
				if (stop) {
					log.info("Ignore topic not allowed by configuration > {} > {}", topicName, topicMessageType);
					continue;
				}
			}

			log.info("Try to subscribe to topic > {} > {}", topicName, topicMessageType);

			switch (topicMessageType) {

			case "std_msgs/Float32":
				// messageCache.put(topicName, null);
				messageTypeCache.put(topicName, topicMessageType);
				subscribeToFloat32(connectedNode, topicName);
				break;

			case "std_msgs/Float32MultiArray":
				// messageCache.put(topicName, null);
				messageTypeCache.put(topicName, topicMessageType);
				subscribeToFloat32MultiArray(connectedNode, topicName);
				break;

			case "std_msgs/Int32":
				// messageCache.put(topicName, null);
				messageTypeCache.put(topicName, topicMessageType);
				subscribeToInt32(connectedNode, topicName);
				break;

			case "std_msgs/String":
				// messageCache.put(topicName, null);
				messageTypeCache.put(topicName, topicMessageType);
				subscribeToString(connectedNode, topicName);
				break;

			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		initialized = true;

	}

	private void updateCaches(String topicName, Set<org.semanticweb.yars.nx.Node[]> set) {

		messageCache.put(topicName, set);

		Cache<String, Set<org.semanticweb.yars.nx.Node[]>> messageQueueCache = messageQueueCaches
				.getIfPresent(topicName);
		if (messageQueueCache == null) {
			messageQueueCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(10000).build();
			messageQueueCaches.put(topicName, messageQueueCache);
		}

		messageQueueCache.put(String.valueOf(System.currentTimeMillis()), set);

	}

	private void subscribeToFloat32(ConnectedNode connectedNode, final String topicName) {

		try {

			Subscriber<std_msgs.Float32> subscriber = connectedNode.newSubscriber(topicName, std_msgs.Float32._TYPE);

			// subscriber.addSubscriberListener(arg0);

			subscriber.addMessageListener(new MessageListener<std_msgs.Float32>() {

				@Override
				public void onNewMessage(std_msgs.Float32 message) {

					updateCaches(topicName, standardMessageSerializer.serializeFloat32ToRdf(topicName, message));

					log.info("std_msgs.Float32 > {} > data={}", topicName, message.getData());

					// if
					// (topicName.equals("/sim/values/cockpit/controls/joystick_pitch_ratio"))
					// {
					//
					// log.info("{} > data={}", topicName, message.getData());
					//
					// }

					// if
					// (topicName.equals("/sim/values/flightmodel/aircraft/position/pitch_accl_degss"))
					// {
					//
					// log.info("{} > data={}", topicName, message.getData());
					//
					// }

				}

			});

		} catch (XmlRpcTimeoutException e) {
			log.warn("ASDF1 > {}", e);
		}

	}

	private void subscribeToFloat32MultiArray(ConnectedNode connectedNode, final String topicName) {

		Subscriber<std_msgs.Float32MultiArray> subscriber = connectedNode.newSubscriber(topicName,
				std_msgs.Float32MultiArray._TYPE);

		subscriber.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {

			@Override
			public void onNewMessage(std_msgs.Float32MultiArray message) {

				updateCaches(topicName, standardMessageSerializer.serializeFloat32MultiArrayToRdf(topicName, message));

				MultiArrayLayout layout = message.getLayout();
				int dataOffset = layout.getDataOffset();
				List<MultiArrayDimension> dims = layout.getDim();
				for (MultiArrayDimension dim : dims) {
					dim.getLabel();
					dim.getSize();
					dim.getStride();
				}

				log.info("std_msgs.Float32MultiArray > {} > layout=[dataOffset={} | dims={}] | data={}", topicName,
						dataOffset, dims, message.getData());

			}

		});

	}

	private void subscribeToInt32(ConnectedNode connectedNode, final String topicName) {

		Subscriber<std_msgs.Int32> subscriber = connectedNode.newSubscriber(topicName, std_msgs.Int32._TYPE);

		subscriber.addMessageListener(new MessageListener<std_msgs.Int32>() {

			@Override
			public void onNewMessage(std_msgs.Int32 message) {

				updateCaches(topicName, standardMessageSerializer.serializeInt32ToRdf(topicName, message));

				log.info("std_msgs.Int32 > {} > data={}", topicName, message.getData());

			}

		});

	}

	private void subscribeToString(ConnectedNode connectedNode, final String topicName) {

		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(topicName, std_msgs.String._TYPE);

		subscriber.addMessageListener(new MessageListener<std_msgs.String>() {

			@Override
			public void onNewMessage(std_msgs.String message) {
				updateCaches(topicName, standardMessageSerializer.serializeStringToRdf(topicName, message));
				log.info("std_msgs.String > {} > data={}", topicName, message.getData());
			}

		});

	}

	public void setMasterClient(MasterClient masterClient) {
		this.masterClient = masterClient;
	}

	public Cache<String, Set<org.semanticweb.yars.nx.Node[]>> getMessageCache() {
		return messageCache;
	}

	public void setMessageCache(Cache<String, Set<org.semanticweb.yars.nx.Node[]>> cache) {
		this.messageCache = cache;
	}

	public Cache<String, String> getMessageTypeCache() {
		return messageTypeCache;
	}

	public void setMessageTypeCache(Cache<String, String> messageTypeCache) {
		this.messageTypeCache = messageTypeCache;
	}

	public Cache<String, Cache<String, Set<org.semanticweb.yars.nx.Node[]>>> getMessageQueueCache() {
		return messageQueueCaches;
	}

	public void setMessageQueueCache(
			Cache<String, Cache<String, Set<org.semanticweb.yars.nx.Node[]>>> messageCacheQueue) {
		this.messageQueueCaches = messageCacheQueue;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}