package net.fekepp.roest.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fekepp.roest.ControllerImplementation;
import net.fekepp.roest.api.servlets.ApiServlet;

@WebListener
public class ControllerContextListener implements ServletContextListener {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ControllerImplementation controller;

	public ControllerContextListener() {
		controller = new ControllerImplementation();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		// Log
		log.info("public void contextInitialized(ServletContextEvent servletContextEvent)");

		// Start controller
		controller.start();

		// Set controller at servlet
		ApiServlet.setController(controller);

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

		// Log
		log.info("public void contextDestroyed(ServletContextEvent servletContextEvent)");

		// Stop controller
		controller.stop();

	}

}