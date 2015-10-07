package net.fekepp.roest.ros;

import java.util.List;

import std_msgs.MultiArrayDimension;
import std_msgs.MultiArrayLayout;

public class StandardMessageSerializer {

	// private final Logger log = LoggerFactory.getLogger(getClass());

	public String serializeFloat32ToRdf(String topicName,
			std_msgs.Float32 message) {
		return "<> <http://ns.fekepp.net/roest#data> \"" + message.getData()
				+ "\"^^<http://www.w3.org/2001/XMLSchema#float> .";
	}

	public String serializeFloat32MultiArrayToRdf(String topicName,
			std_msgs.Float32MultiArray message) {

		MultiArrayLayout layout = message.getLayout();
		// int dataOffset = layout.getDataOffset();
		List<MultiArrayDimension> dims = layout.getDim();
		for (MultiArrayDimension dim : dims) {
			dim.getLabel();
			dim.getSize();
			dim.getStride();
		}

		// log.info("{} > layout=[dataOffset={} | dims={}] | data={}",
		// topicName,
		// dataOffset, dims, message.getData());

		String data = "[";
		for (float entry : message.getData()) {
			data += entry + ",";
		}
		if (data.length() > 1) {
			data.substring(0, data.length() - 1);
		}
		data += "]";

		return "<> <http://ns.fekepp.net/roest#data> \"" + data + "\" .";

	}

	public String serializeInt32ToRdf(String topicName, std_msgs.Int32 message) {
		return "<> <http://ns.fekepp.net/roest#data> \"" + message.getData()
				+ "\"^^<http://www.w3.org/2001/XMLSchema#integer> .";

	}

	public String serializeStringToRdf(String topicName, std_msgs.String message) {
		return "<> <http://ns.fekepp.net/roest#data> \"" + message.getData()
				+ "\" .";
	}

}