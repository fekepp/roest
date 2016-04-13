package net.fekepp.gradle.stub.servlets;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.LDP;
import org.semanticweb.yars.nx.namespace.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;

import net.fekepp.roest.ControllerImplementation;

@Path("/{identifier: .*}")
public class RootServlet {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String queueSuffix = "queue";

	private static ControllerImplementation controller;

	private Cache<String, Set<Node[]>> messageCache;
	private Cache<String, Cache<String, Set<Node[]>>> messageQueueCaches;

	@Context
	private ServletContext context;

	@Context
	UriInfo uriInfo;

	public RootServlet() {
		messageCache = controller.getMessageCache();
		messageQueueCaches = controller.getMessageQueueCache();
	}

	@GET
	public Response getRepresentation(@PathParam("identifier") String identifier) {

		// Representation to be returned
		Set<Node[]> representation = new HashSet<Node[]>();

		// URI of the requested resource
		Resource identifierUri = new Resource(uriInfo.getRequestUri().toString());

		// Split identifier to be used in subsequent steps
		String[] identifierSplit = identifier.split("/");

		// Return representation of root container
		if (identifier.equals("")) {

			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.CONTAINER });
			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.BASIC_CONTAINER });

			for (String subIdentifierKey : messageCache.asMap().keySet()) {
				representation.add(new Node[] { identifierUri, LDP.CONTAINS,
						new Resource(uriInfo.getRequestUri().toString() + subIdentifierKey.replaceFirst("/", "")) });
			}

		}

		// Return representation of a queue container
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 1].equals(queueSuffix)) {

			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.CONTAINER });
			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.BASIC_CONTAINER });

			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifier.substring(0, identifier.length() - queueSuffix.length() - 1));

			if (messageQueueCache == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}

			for (String subSubIdentifierKey : messageQueueCache.asMap().keySet()) {
				representation.add(new Node[] { identifierUri, LDP.CONTAINS,
						new Resource(uriInfo.getRequestUri().toString() + "/" + subSubIdentifierKey) });
			}

		}

		// Return representation of a resource from a queue container
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 2].equals(queueSuffix)) {

			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifierSplit[identifierSplit.length - 3]);

			if (messageQueueCache == null) {
				throw new NotFoundException();
			}

			representation.addAll(messageQueueCache.getIfPresent(identifierSplit[identifierSplit.length - 1]));

		}

		// Return representation of a topic resource
		else if (identifierSplit.length > 0) {

			logger.info("identifier > {} | identifierSplit[identifierSplit.length - 1] > {}", identifier,
					identifierSplit[identifierSplit.length - 1]);

			// Set<Node[]> message = messageCache.getIfPresent("/" +
			// identifierSplit[identifierSplit.length - 1]);
			Set<Node[]> message = messageCache.getIfPresent("/" + identifier);

			if (message == null) {
				logger.info("______________________________________");
				ConcurrentMap<String, Set<Node[]>> map = messageCache.asMap();
				for (String key : map.keySet()) {
					logger.info("key > {} | value > {}", key, map.get(key));
				}

				logger.info("______________________________________");
				throw new NotFoundException();
			}

			representation.addAll(message);

		}

		// Something went wrong, this should not happen
		else {

			// Throw internal server error
			throw new WebApplicationException("Could not resolve resource", 500);

		}

		// Return representation
		return Response.ok(new GenericEntity<Iterable<Node[]>>(representation) {
		}).build();

	}

	@DELETE
	public Response deleteRepresentation(@PathParam("identifier") String identifier) {

		// Split identifier to be used in subsequent steps
		String[] identifierSplit = identifier.split("/");

		// Deletion of root container not allowed
		if (identifier.equals("")) {
			throw new NotAllowedException("Deletion not allowed");
		}

		// Deletion of queue containers not allowed
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 1].equals(queueSuffix)) {
			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifier.substring(0, identifier.length() - queueSuffix.length() - 1));
			if (messageQueueCache == null) {
				throw new NotFoundException();
			}
			throw new NotAllowedException("Deletion not allowed");
		}

		// Delete resource from a container queue
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 2].equals(queueSuffix)) {

			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifierSplit[identifierSplit.length - 3]);

			if (messageQueueCache == null) {
				throw new NotFoundException();
			}

			// Delete resource in queue by invalidating respective cache entry
			messageQueueCache.invalidate(identifierSplit[identifierSplit.length - 1]);

		}

		// Deletion of topic resources not allowed
		else if (identifierSplit.length > 0) {
			Set<Node[]> message = messageCache.getIfPresent("/" + identifier);
			if (message == null) {
				throw new NotFoundException();
			}
			throw new NotAllowedException("Deletion not allowed");
		}

		// Something went wrong, this should not happen
		else {

			// Throw internal server error
			throw new WebApplicationException("Could not resolve resource", 500);

		}

		// Return empty content to client
		return Response.noContent().build();

	}

	public static void setController(ControllerImplementation controller) {
		RootServlet.controller = controller;
	}

}