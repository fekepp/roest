package net.fekepp.roest.ros;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.XSD;

import std_msgs.MultiArrayDimension;
import std_msgs.MultiArrayLayout;

public class StandardMessageSerializer {

	// private final Logger logger = LoggerFactory.getLogger(getClass());

	public Set<Node[]> serializeFloat32ToRdf(String topicName, std_msgs.Float32 message) {

		Set<Node[]> representation = new HashSet<Node[]>();

		// <>
		// <http://ns.fekepp.net/roest#data> "[message.getData()]"
		// ^^<http://www.w3.org/2001/XMLSchema#float>
		// .
		representation.add(new Node[] { new Resource(""), new Resource("http://roest#data"),
				new Literal(String.valueOf(message.getData()), XSD.FLOAT) });

		return representation;

	}

	public Set<Node[]> serializeFloat32MultiArrayToRdf(String topicName, std_msgs.Float32MultiArray message) {

		Set<Node[]> representation = new HashSet<Node[]>();

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

		// <>
		// <http://ns.fekepp.net/roest#data>
		// "[data]"
		// .
		representation.add(
				new Node[] { new Resource(""), new Resource("http://roest#data"), new Literal(String.valueOf(data)) });

		return representation;

	}

	public Set<Node[]> serializeInt32ToRdf(String topicName, std_msgs.Int32 message) {

		Set<Node[]> representation = new HashSet<Node[]>();

		// <>
		// <http://ns.fekepp.net/roest#data> "[message.getData()]"
		// ^^<http://www.w3.org/2001/XMLSchema#integer>
		// .
		representation.add(new Node[] { new Resource(""), new Resource("http://roest#data"),
				new Literal(String.valueOf(message.getData()), XSD.INTEGER) });

		return representation;

	}

	public Set<Node[]> serializeStringToRdf(String topicName, std_msgs.String message) {

		Set<Node[]> representation = new HashSet<Node[]>();

		// <>
		// <http://ns.fekepp.net/roest#data> "[message.getData()]"
		// .
		representation.add(new Node[] { new Resource(""), new Resource("http://roest#data"),
				new Literal(String.valueOf(message.getData())) });

		return representation;

	}

}