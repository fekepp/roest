package net.fekepp.gradle.stub;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;

import net.fekepp.roest.ControllerDelegate;
import net.fekepp.roest.ControllerImplementation;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class ResourceConfigJersey extends ResourceConfig implements
		ControllerDelegate {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// private static ControllerImplementation controller;

	private static ConcurrentMap<String, String> messageCache;

	private static ConcurrentMap<String, String> messageTypeCache;

	// @Context
	// private ServletContext context;

	public ResourceConfigJersey() {

		packages(getClass().getPackage().getName() + ".servlets");

		// if (controller == null) {
		// controller = new ControllerImplementation();
		// controller.setControllerDelegate(this);
		// controller.start();
		// messageCache = controller.getMessageCache();
		// messageTypeCache = controller.getMessageTypeCache();
		//
		// }

		// test();
		// if (context == null) {
		// log.error("TEST > ServletContext is null!");
		// } else {
		// log.error("TEST > ServletContext is not null!");
		// }

	}

	private void buildInterface() {

		log.info("messageCache.size() > {}", messageCache.size());
		log.info("messageTypeCache.size() > {}", messageTypeCache.size());

		for (final Entry<String, String> entry : messageTypeCache.entrySet()) {

			// Get the resource builder
			final Resource.Builder resourceBuilder = Resource.builder();
			resourceBuilder.path(entry.getKey());

			// Get method builder for HTTP GET
			ResourceMethod.Builder childMethodBuilderGet = resourceBuilder
					.addMethod("GET");

			// Add a method handler for procuing TEXT_HTML
			childMethodBuilderGet.produces(MediaType.TEXT_HTML).handledBy(
					new Inflector<ContainerRequestContext, String>() {

						@Override
						public String apply(
								ContainerRequestContext containerRequestContext) {

							return messageCache.get(entry.getKey())
									.replace("<", "&lt;").replace(">", "&gt;");

						}

					});

			// Add a method handler for procuing TEXT_TURTLE
			childMethodBuilderGet.produces(MediaTypeLocal.TEXT_TURTLE)
					.handledBy(
							new Inflector<ContainerRequestContext, String>() {

								@Override
								public String apply(
										ContainerRequestContext containerRequestContext) {

									return "Test";

								}

							});

			// Build the resource
			final Resource resource = resourceBuilder.build();

			// Register the resource
			registerResources(resource);

			log.info("Registered resource > {}", entry.getKey());

		}

	}

	private void test() {

		// Get the resource builder
		final Resource.Builder resourceBuilder = Resource.builder();
		resourceBuilder.path("test");

		// Resource.Builder childResourceBuilder = resourceBuilder
		// .addChildResource(directory.getName());

		// Get method builder for HTTP GET
		ResourceMethod.Builder childMethodBuilder = resourceBuilder
				.addMethod("GET");

		childMethodBuilder.produces(MediaType.TEXT_HTML).handledBy(
				new Inflector<ContainerRequestContext, String>() {

					@Override
					public String apply(
							ContainerRequestContext containerRequestContext) {

						return "Test";
					}

				});

		// Build the resource
		final Resource resource = resourceBuilder.build();

		// Register the resource
		registerResources(resource);

	}

	@Override
	public void onControllerStarted() {
		buildInterface();
	}

	@Override
	public void onControllerStopped() {
		log.info("public void onControllerStopped()");
	}
}