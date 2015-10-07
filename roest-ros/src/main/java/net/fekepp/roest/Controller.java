package net.fekepp.roest;

public interface Controller {

	public abstract void start();

	public abstract void stop();

	public abstract void run();

	public abstract boolean isStarted();

	public abstract void setControllerDelegate(ControllerDelegate delegate);

}