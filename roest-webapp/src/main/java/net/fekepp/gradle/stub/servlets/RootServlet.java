package net.fekepp.gradle.stub.servlets;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
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
	public Response getRDFSource(@PathParam("identifier") String identifier) {

		// Representation to be returned
		Set<Node[]> representation = new HashSet<Node[]>();

		// URI of the requested resource
		Resource identifierUri = new Resource(uriInfo.getRequestUri().toString());

		// Split identifier to distinguish between different requested resources
		// in subsequent steps
		String[] identifierSplit = identifier.split("/");

		// Return representation of root container with all identifiers
		if (identifier.equals("")) {

			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.CONTAINER });
			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.BASIC_CONTAINER });

			for (String subIdentifierKey : messageCache.asMap().keySet()) {
				representation.add(new Node[] { identifierUri, LDP.CONTAINS,
						new Resource(uriInfo.getRequestUri().toString() + subIdentifierKey.replaceFirst("/", "")) });
			}

		}

		// Return container representation of a specific identifier's queue
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 1].equals("queue")) {

			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.CONTAINER });
			representation.add(new Node[] { identifierUri, RDF.TYPE, LDP.BASIC_CONTAINER });

			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifierSplit[identifierSplit.length - 2]);

			if (messageQueueCache == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}

			for (String subSubIdentifierKey : messageQueueCache.asMap().keySet()) {
				representation.add(new Node[] { identifierUri, LDP.CONTAINS,
						new Resource(uriInfo.getRequestUri().toString() + "/" + subSubIdentifierKey) });
			}

		}

		// Return representation of a resource from a container of a specific
		// identifier's queue
		else if (identifierSplit.length > 1 && identifierSplit[identifierSplit.length - 2].equals("queue")) {

			Cache<String, Set<Node[]>> messageQueueCache = messageQueueCaches
					.getIfPresent("/" + identifierSplit[identifierSplit.length - 3]);

			if (messageQueueCache == null) {
				throw new NotFoundException();
			}

			representation.addAll(messageQueueCache.getIfPresent(identifierSplit[identifierSplit.length - 1]));

		}

		// Return representation of identifier's resource
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

			// Throw interal server error
			throw new WebApplicationException("Could not resolve resource", 500);

		}

		// Return representation
		return Response.ok(new GenericEntity<Iterable<Node[]>>(representation) {
		}).build();

	}

	public static void setController(ControllerImplementation controller) {
		RootServlet.controller = controller;
	}

}