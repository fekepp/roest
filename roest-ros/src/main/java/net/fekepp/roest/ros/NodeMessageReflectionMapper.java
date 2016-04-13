package net.fekepp.roest.ros;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ros.internal.message.RawMessage;
import org.ros.internal.message.field.Field;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.TopicType;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.fekepp.roest.Configuration;

public class NodeMessageReflectionMapper implements NodeMain {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Cache<String, Set<org.semanticweb.yars.nx.Node[]>> messageCache;
	private Cache<String, String> messageTypeCache;
	private Cache<String, Cache<String, Set<org.semanticweb.yars.nx.Node[]>>> messageQueueCaches;

	private MasterClient masterClient;

	private boolean initialized;

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("roest/stdmsg");
	}

	public MasterClient getMasterClient() {
		return masterClient;
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		logger.info("public void onError(Node node, Throwable throwable)");
	}

	@Override
	public void onShutdown(Node node) {
		logger.info("public void onShutdown(Node node)");
	}

	@Override
	public void onShutdownComplete(Node node) {
		logger.info("public void onShutdownComplete(Node node)");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		Response<List<TopicType>> response = masterClient.getTopicTypes(this.getDefaultNodeName());

		String[] allowedTopicNames = Configuration.getTopicNames();
		logger.info("Number of allowed topics > {}", allowedTopicNames.length);

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
					logger.info("Available topic > DISALLOWED > {} > {}", topicName, topicMessageType);
					continue;
				}
			}

			logger.info("Available topic > ALLOWED > {} > {}", topicName, topicMessageType);

			switch (topicMessageType) {

			case "ivision/Interaction":
				break;

			default:
				messageTypeCache.put(topicName, topicMessageType);
				subscribeToMessage(connectedNode, topicName, topicMessageType);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void subscribeToMessage(ConnectedNode connectedNode, String topicName, String topicMessageType) {

		Subscriber subscriber = connectedNode.newSubscriber(topicName, topicMessageType);

		subscriber.addMessageListener(new MessageListener<Object>() {

			@Override
			public void onNewMessage(Object message) {

				Set<org.semanticweb.yars.nx.Node[]> representation = generateRepresentation(message);

				messageCache.put(topicName, representation);

				Cache<String, Set<org.semanticweb.yars.nx.Node[]>> messageQueueCache = messageQueueCaches
						.getIfPresent(topicName);
				if (messageQueueCache == null) {
					messageQueueCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(10000)
							.build();
					messageQueueCaches.put(topicName, messageQueueCache);
				}

				messageQueueCache.put(String.valueOf(System.currentTimeMillis()), representation);

			}

		});

	}

	private Set<org.semanticweb.yars.nx.Node[]> generateRepresentation(Object message) {

		Set<org.semanticweb.yars.nx.Node[]> representation = new HashSet<org.semanticweb.yars.nx.Node[]>();

		Class<? extends Object> clazzMessage = message.getClass();
		// logger.info("TEST > clazz > {}", clazzMessage);

		// for (Method method : clazzRawMessage.getDeclaredMethods()) {
		// logger.info("TEST > method > {}", method);
		// }

		try {

			// Method methodToString = clazz.getMethod("toString");
			// logger.info("TEST > methodToString > {}", methodToString);
			// methodToString.setAccessible(true);
			// Object invokationToString = methodToString.invoke(message);
			// logger.info("TEST > invokationToString > {}",
			// invokationToString);

			// Method methodGetData = clazz.getMethod("getData");
			// logger.info("TEST > methodGetData > {}", methodGetData);
			// methodGetData.setAccessible(true);
			// Object invokationGetData = methodGetData.invoke(message);
			// logger.info("TEST > invokationGetData > {}", invokationGetData);

			Method methodtoRawMessage = clazzMessage.getMethod("toRawMessage");
			// logger.info("TEST > methodtoRawMessage > {}",
			// methodtoRawMessage);
			methodtoRawMessage.setAccessible(true);
			RawMessage invocationRawMessage = (RawMessage) methodtoRawMessage.invoke(message);
			// logger.info("TEST > = invocationRawMessage > {}",
			// = invocationRawMessage);

			// Class<? extends Object> clazzRawMessage =
			// = invocationRawMessage.getClass();
			// for (Method method : clazzRawMessage.getDeclaredMethods()) {
			// logger.info("TEST > method > {}", method);
			// }

			// logger.info("__________________________________________________");
			// logger.info("IDENTIFIER > {}",
			// invocationRawMessage.getIdentifier());
			// logger.info("NAME > {}", invocationRawMessage.getName());
			// logger.info("TYPE > {}", invocationRawMessage.getType());
			// logger.info("__________________________________________________");
			// logger.info("DEFINITION >\n{}",
			// invocationRawMessage.getDefinition());
			// logger.info("__________________________________________________");

			// boolean getBool = invocationRawMessage.getBool("test");
			// boolean[] getBoolArray =
			// invocationRawMessage.getBoolArray("test");
			// byte getByte = invocationRawMessage.getByte("test");
			// byte[] getByteArray = invocationRawMessage.getByteArray("test");
			// ChannelBuffer getChannelBuffer =
			// invocationRawMessage.getChannelBuffer("test");
			// short getChar = invocationRawMessage.getChar("test");
			// short[] getCharArray = invocationRawMessage.getCharArray("test");
			// Class<? extends RawMessage> getClass =
			// invocationRawMessage.getClass();
			// String getDefinition = invocationRawMessage.getDefinition();
			// Duration getDuration = invocationRawMessage.getDuration("test");
			// List<Duration> getDurationList =
			// invocationRawMessage.getDurationList("test");
			// List<Field> getFields = invocationRawMessage.getFields();
			// float getFloat32 = invocationRawMessage.getFloat32("test");
			// float[] getFloat32Array =
			// invocationRawMessage.getFloat32Array("test");
			// double getFloat64 = invocationRawMessage.getFloat64("test");
			// double[] getFloat64Array =
			// invocationRawMessage.getFloat64Array("test");
			// MessageIdentifier getIdentifier =
			// invocationRawMessage.getIdentifier();
			// short getInt16 = invocationRawMessage.getInt16("test");
			// short[] getInt16Array =
			// invocationRawMessage.getInt16Array("test");
			// int getInt32 = invocationRawMessage.getInt32("test");
			// int[] getInt32Array = invocationRawMessage.getInt32Array("test");
			// long getInt64 = invocationRawMessage.getInt64("test");
			// long[] getInt64Array =
			// invocationRawMessage.getInt64Array("test");
			// byte getInt8 = invocationRawMessage.getInt8("test");
			// byte[] getInt8Array = invocationRawMessage.getInt8Array("test");
			// Message getMessage = invocationRawMessage.getMessage("test");
			// List<Message> getMessageList =
			// invocationRawMessage.getMessageList("test");
			// String getName = invocationRawMessage.getName();
			// String getPackage = invocationRawMessage.getPackage();
			// String getString = invocationRawMessage.getString("test");
			// List<String> getStringList =
			// invocationRawMessage.getStringList("test");
			// Time getTime = invocationRawMessage.getTime("test");
			// List<Time> getTimeList =
			// invocationRawMessage.getTimeList("test");
			// String getType = invocationRawMessage.getType();
			// short getUInt16 = invocationRawMessage.getUInt16("test");
			// short[] getUInt16Array =
			// invocationRawMessage.getUInt16Array("test");
			// int getUInt32 = invocationRawMessage.getUInt32("test");
			// int[] getUInt32Array =
			// invocationRawMessage.getUInt32Array("test");
			// long getUInt64 = invocationRawMessage.getUInt64("test");
			// long[] getUInt64Array =
			// invocationRawMessage.getUInt64Array("test");
			// short getUInt8 = invocationRawMessage.getUInt8("test");
			// short[] getUInt8Array =
			// invocationRawMessage.getUInt8Array("test");

			List<Field> messageFields = invocationRawMessage.getFields();
			for (Field field : messageFields) {

				// logger.info("{} > {} > {} > {}", field.getType(),
				// field.getJavaTypeName(), field.getName(),
				// field.getValue());
				Literal literal = null;

				switch (field.getJavaTypeName()) {

				case "boolean":
					literal = new Literal(String.valueOf((boolean) field.getValue()), XSD.BOOLEAN);
					break;

				case "byte":
					literal = new Literal(String.valueOf((byte) field.getValue()), XSD.BYTE);
					break;

				case "short":
					literal = new Literal(String.valueOf((short) field.getValue()), XSD.SHORT);
					break;

				case "int":
					literal = new Literal(String.valueOf((int) field.getValue()), XSD.INT);
					break;

				case "long":
					literal = new Literal(String.valueOf((long) field.getValue()), XSD.LONG);
					break;

				case "float":
					literal = new Literal(String.valueOf((float) field.getValue()), XSD.FLOAT);
					break;

				case "double":
					literal = new Literal(String.valueOf((double) field.getValue()), XSD.DOUBLE);
					break;

				case "java.lang.String":
					literal = new Literal((String) field.getValue());
					break;

				}

				if (literal != null) {
					representation.add(new org.semanticweb.yars.nx.Node[] { new Resource(""),
							new Resource("http://roest#" + field.getName()), literal });
				}

			}
			// logger.info("__________________________________________________");

		} catch (NoSuchMethodException e) {
			logger.error("EXCEPTION 01", e);
		} catch (SecurityException e) {
			logger.error("EXCEPTION 02", e);
		} catch (IllegalAccessException e) {
			logger.error("EXCEPTION 03", e);
		} catch (IllegalArgumentException e) {
			logger.error("EXCEPTION 04", e);
		} catch (InvocationTargetException e) {
			logger.error("EXCEPTION 05", e);
		}

		return representation;
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