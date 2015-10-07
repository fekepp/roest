package net.fekepp.roest.ros;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.TopicType;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import std_msgs.MultiArrayDimension;
import std_msgs.MultiArrayLayout;

public class StandardMessageNode implements NodeMain {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ConcurrentMap<String, String> messageCache;
	private ConcurrentMap<String, String> messageTypeCache;

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

		Response<List<TopicType>> response = masterClient.getTopicTypes(this
				.getDefaultNodeName());

		// StatusCode statusCode = response.getStatusCode();
		// int statusCodeInt = statusCode.toInt();
		// String statusCodeString = statusCode.toString();
		List<TopicType> result = response.getResult();

		for (TopicType topicType : result) {

			String topicName = topicType.getName();
			String topicMessageType = topicType.getMessageType();

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

		// subscribeToFloat32(connectedNode,
		// "sim/values/flightmodel/aircraft/position/verticalspeed_indicated_kts");
		//
		// subscribeToFloat32MultiArray(connectedNode,
		// "sim/values/flightmodel/aircraft/position/position");
		//
		// subscribeToInt32(connectedNode, "sim/time/utc_time");
		//
		// subscribeToString(connectedNode,
		// "sim/values/flightmodel/aircraft/position/airport");

		// subscribeToTopic(
		// connectedNode,
		// "sim/values/flightmodel/aircraft/position/verticalspeed_indicated_kts",
		// std_msgs.Float32._TYPE);

		initialized = true;

	}

	private void subscribeToFloat32(ConnectedNode connectedNode,
			final String topicName) {

		Subscriber<std_msgs.Float32> subscriber = connectedNode.newSubscriber(
				topicName, std_msgs.Float32._TYPE);

		// subscriber.addSubscriberListener(arg0);

		subscriber.addMessageListener(new MessageListener<std_msgs.Float32>() {

			@Override
			public void onNewMessage(std_msgs.Float32 message) {

				messageCache.put(topicName, standardMessageSerializer
						.serializeFloat32ToRdf(topicName, message));

				// log.info("{} > data={}", topicName, message.getData());

			}

		});

	}

	private void subscribeToFloat32MultiArray(ConnectedNode connectedNode,
			final String topicName) {

		Subscriber<std_msgs.Float32MultiArray> subscriber = connectedNode
				.newSubscriber(topicName, std_msgs.Float32MultiArray._TYPE);

		subscriber
				.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {

					@Override
					public void onNewMessage(std_msgs.Float32MultiArray message) {

						messageCache.put(topicName, standardMessageSerializer
								.serializeFloat32MultiArrayToRdf(topicName,
										message));

						MultiArrayLayout layout = message.getLayout();
						int dataOffset = layout.getDataOffset();
						List<MultiArrayDimension> dims = layout.getDim();
						for (MultiArrayDimension dim : dims) {
							dim.getLabel();
							dim.getSize();
							dim.getStride();
						}

						// log.info(
						// "{} > layout=[dataOffset={} | dims={}] | data={}",
						// topicName, dataOffset, dims, message.getData());

					}

				});

	}

	private void subscribeToInt32(ConnectedNode connectedNode,
			final String topicName) {

		Subscriber<std_msgs.Int32> subscriber = connectedNode.newSubscriber(
				topicName, std_msgs.Int32._TYPE);

		subscriber.addMessageListener(new MessageListener<std_msgs.Int32>() {

			@Override
			public void onNewMessage(std_msgs.Int32 message) {

				messageCache.put(topicName, standardMessageSerializer
						.serializeInt32ToRdf(topicName, message));

				// log.info("{} > data={}", topicName, message.getData());

			}

		});

	}

	private void subscribeToString(ConnectedNode connectedNode,
			final String topicName) {

		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(
				topicName, std_msgs.String._TYPE);

		subscriber.addMessageListener(new MessageListener<std_msgs.String>() {

			@Override
			public void onNewMessage(std_msgs.String message) {
				messageCache.put(topicName, standardMessageSerializer
						.serializeStringToRdf(topicName, message));
				// log.info("{} > data={}", topicName, message.getData());
			}

		});

	}

	public void setMasterClient(MasterClient masterClient) {
		this.masterClient = masterClient;
	}

	public ConcurrentMap<String, String> getMessageCache() {
		return messageCache;
	}

	public void setMessageCache(ConcurrentMap<String, String> cache) {
		this.messageCache = cache;
	}

	public ConcurrentMap<String, String> getMessageTypeCache() {
		return messageTypeCache;
	}

	public void setMessageTypeCache(
			ConcurrentMap<String, String> messageTypeCache) {
		this.messageTypeCache = messageTypeCache;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
