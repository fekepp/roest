package net.fekepp.roest.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fekepp.roest.Configuration;
import net.fekepp.roest.ControllerDelegate;
import net.fekepp.roest.api.ControllerContextListener;
import net.fekepp.roest.api.servlets.ApiServlet;

/**
 * @author "Felix Leif Keppmann"
 */
public class ServerController extends BaseJettyJerseyController implements ControllerDelegate {

	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void startup() {

		logger.info("public void startup()");

		// Delegate on its own
		setDelegate(this);

		// Register listeners
		getWebAppContext().addEventListener(new ControllerContextListener());

		// Register providers

		// Register servlets
		getResourceConfig().register(ApiServlet.class);

		// Continue startup
		super.startup();

	}

	@Override
	public void shutdown() {
		logger.info("public void shutdown()");
		super.shutdown();
	}

	@Override
	public void onControllerStarted() {
		logger.info("public void onControllerStarted()");
	}

	@Override
	public void onControllerStopped() {
		logger.info("public void onControllerStopped()");
	}

	@Override
	public void onControllerStartupException(Exception e) {
		logger.error("public void onControllerStartupException(Exception e)", e);
	}

	@Override
	public void onControllerRunException(Exception e) {
		logger.error("public void onControllerRunException(Exception e)", e);
	}

	@Override
	public void onControllerShutdownException(Exception e) {
		logger.error("public void onControllerShutdownException(Exception e)", e);
	}

}