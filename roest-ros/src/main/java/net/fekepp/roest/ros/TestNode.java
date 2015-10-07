package net.fekepp.roest.ros;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNode implements NodeMain {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void onError(Node node, Throwable throwable) {
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

		Subscriber<turtlesim.Pose> subscriber = connectedNode.newSubscriber(
				"/turtle1/pose", turtlesim.Pose._TYPE);

		subscriber.addMessageListener(new MessageListener<turtlesim.Pose>() {

			@Override
			public void onNewMessage(turtlesim.Pose message) {
				log.info(
						"x={} | y={} | theta={} | linear_velocity={} | angular_velocity={}",
						message.getX(), message.getY(), message.getTheta(),
						message.getLinearVelocity(),
						message.getAngularVelocity());
			}

		});

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("roest/test");
	}

}