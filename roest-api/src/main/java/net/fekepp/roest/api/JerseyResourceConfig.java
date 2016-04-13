package net.fekepp.roest.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class JerseyResourceConfig extends ResourceConfig {

	public JerseyResourceConfig() {
		packages(getClass().getPackage().getName() + ".servlets");
	}

}