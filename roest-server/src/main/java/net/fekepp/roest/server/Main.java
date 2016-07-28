package net.fekepp.roest.server;

import net.fekepp.roest.Configuration;

public class Main {

	public static void main(String[] args) {
		ServerController controller = new ServerController();
		controller.setHost(Configuration.getHost());
		controller.setPort(Configuration.getPort());
		controller.startBlocking();
	}

}