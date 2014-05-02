package hw13;

import java.util.concurrent.locks.ReentrantLock;

public class PiechartObserver implements Observer {
	private ReentrantLock lock = new ReentrantLock();

	@Override
	public void update(Observable o, Object arg) {
		lock.lock();
		System.out.println("Piechart is updated with "+arg);
		lock.unlock();
	}

}
