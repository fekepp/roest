package net.fekepp.roest.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class ResourceConfigJersey extends ResourceConfig {

	public ResourceConfigJersey() {
		packages(getClass().getPackage().getName() + ".servlets");
	}

}