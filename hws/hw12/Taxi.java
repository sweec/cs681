package hw12;

import java.awt.Point;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Taxi implements Runnable {
	private Point location;
	private Point dest;
	private Dispatcher dispatcher;
	private ReentrantLock lock = new ReentrantLock();
	
	public Taxi(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public Point getLocation() {
		Point loc;
		lock.lock();
		loc = location;
		lock.unlock();
		return loc;
	}
	
	public void setLocation(Point loc) {
		lock.lock();
		try {
			location = loc;
			if (!location.equals(dest))
				return;
		} finally {
			lock.unlock();
		}
		dispatcher.notifyAvailable(this);
	}
	
	@Override
	public void run() {
		while (true) {
			setLocation(getGPSLoc());
		}
	}

	private Point getGPSLoc() {
		Random g = new Random();
		int n = 100;
		return new Point(g.nextInt(n), g.nextInt(n));
	}

}
