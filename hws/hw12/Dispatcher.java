package hw12;

import java.awt.Point;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

public class Dispatcher {
	private HashSet<Taxi> taxis;
	private HashSet<Taxi> availableTaxis;
	private Display display = new Display();
	private ReentrantLock lock = new ReentrantLock();

	public void notifyAvailable(Taxi taxi) {
		lock.lock();
		availableTaxis.add(taxi);
		lock.unlock();
	}

	public void displayAvailableTaxis() {
		HashSet<Taxi> availableTaxisLocal;
		lock.lock();
		availableTaxisLocal = availableTaxis;
		lock.unlock();
		for (Taxi t: availableTaxisLocal)
			display.draw(t.getLocation());
	}
	
	private class Display {
		public void draw(Point loc) {
			System.out.println("Location: "+loc);
		}
	}
}
