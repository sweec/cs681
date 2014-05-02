package hw12;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Observable {
	protected ArrayList<Observer> observers = new ArrayList<Observer>();
	private boolean changed = false;
	protected ReentrantLock lock = new ReentrantLock();
	
	public void addObserver(Observer o) {
		lock.lock();
		observers.add(o);
		lock.unlock();
	}
	
	public void setChanged() {
		changed = true;
	}
	
	public boolean hasChanged() {
		return changed;
	}
	
	public void clearChanged() {
		changed = false;
	}
	
	public void notifyObservers() {
		notifyObservers(null);
	}
	
	public void notifyObservers(Object arg) {
		ArrayList<Observer> observersLocal = null;
		lock.lock();
		try {
			if (hasChanged())
				observersLocal = new ArrayList<Observer>(observers);
			else
				return;
			clearChanged();
		} finally {
			lock.unlock();
		}
		for (Observer o:observersLocal)
			o.update(this, arg);
	}
}
