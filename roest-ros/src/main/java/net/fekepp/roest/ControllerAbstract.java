package net.fekepp.roest;

public abstract class ControllerAbstract implements Runnable, Controller {

	private final Object sync = new Object();
	private boolean started = false;
	private ControllerDelegate delegate;

	@Override
	public synchronized void start() {

		if (started) {
			return;
		}

		started = true;

		Thread thread = new Thread(this);
		thread.setName(getClass().getSimpleName().toLowerCase());
		thread.start();

	}

	@Override
	public synchronized void stop() {

		if (!started) {
			return;
		}

		started = false;

		synchronized (sync) {
			sync.notify();
		}

	}

	@Override
	public void run() {

		startup();

		if (delegate != null) {
			delegate.onControllerStarted();
		}

		try {

			synchronized (sync) {
				while (started) {
					sync.wait();
				}
			}

		}

		catch (InterruptedException e) {

		}

		shutdown();

		if (delegate != null) {
			delegate.onControllerStopped();
		}

	}

	@Override
	public synchronized boolean isStarted() {
		return started;
	}

	@Override
	public void setControllerDelegate(ControllerDelegate delegate) {
		this.delegate = delegate;
	}

	protected abstract void startup();

	protected abstract void shutdown();

}
