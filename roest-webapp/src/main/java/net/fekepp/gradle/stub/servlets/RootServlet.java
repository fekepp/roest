package net.fekepp.gradle.stub.servlets;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.fekepp.gradle.stub.MediaTypeLocal;
import net.fekepp.roest.ControllerImplementation;

@Path("/")
public class RootServlet {

	private static ControllerImplementation controller;

	private ConcurrentMap<String, String> messageCache;
	private ConcurrentMap<String, String> messageTypeCache;

	@Context
	private ServletContext context;

	public RootServlet() {
		messageCache = controller.getMessageCache();
		messageTypeCache = controller.getMessageTypeCache();
	}

	// @Path("/")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String overview() {

		StringBuffer buffer = new StringBuffer();
		buffer.append("<html><head><title>ROEST</title></head><body><h1>ROS-REST Gateway (ROEST)</h1>");
		buffer.append("<h2>Listing ROS Topic Resources:</h2>");

		for (Entry<String, String> entry : messageTypeCache.entrySet()) {
			String messageValue = messageCache.get(entry.getKey());
			if (messageValue != null) {
				messageValue = messageValue.replace("<", "&lt;").replace(">",
						"&gt;");
			} else {
				messageValue = "(empty)";
			}

			buffer.append("<a href=\"/roest-webapp").append(entry.getKey())
					.append("\">").append(entry.getKey()).append("</a> [")
					.append(entry.getValue())
					// .append("]<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
					.append("]: ").append(messageValue)
					// .append("<br/><br/>");
					.append("<br/>");
		}

		buffer.append("</body></html>");
		return buffer.toString();

		// return
		// "<html><head><title>ROEST</title></head><body><h1>ROS-REST Gateway (ROEST)</h1></body></html>";

	}

	@Path("/{resource : .*}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String resourceHtml(@PathParam("resource") String resource) {

		String name = "/" + resource;
		String message = messageCache.get(name);

		if (message != null) {
			message = message.replace("<", "&lt;").replace(">", "&gt;");
		}

		else {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return message;
	}

	@Path("/{resource : .*}")
	@GET
	@Produces(MediaTypeLocal.TEXT_TURTLE)
	public String resourceTurtle(@PathParam("resource") String resource) {

		String name = "/" + resource;
		String message = messageCache.get(name);

		if (message == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return message;

	}

	public static void setController(ControllerImplementation controller) {
		RootServlet.controller = controller;
	}

}