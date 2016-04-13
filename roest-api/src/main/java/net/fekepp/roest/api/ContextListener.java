package net.fekepp.roest.api;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import net.fekepp.roest.ControllerImplementation;
import net.fekepp.roest.api.servlets.RootServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ContextListener implements ServletContextListener {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ControllerImplementation controller;

	public ContextListener() {
		controller = new ControllerImplementation();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		log.info("public void contextInitialized(ServletContextEvent servletContextEvent)");
		controller.start();

		RootServlet.setController(controller);

		ServletContext context = servletContextEvent.getServletContext();
		context.setAttribute("controller", controller);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		log.info("public void contextDestroyed(ServletContextEvent servletContextEvent)");
		controller.stop();
	}

}