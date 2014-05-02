package hw13;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Observable {
	protected CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<Observer>();
	private boolean changed = false;
	protected ReentrantLock lock = new ReentrantLock();
	
	public void addObserver(Observer o) {
		observers.add(o);
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
		lock.lock();
		try {
			if (!hasChanged())
				return;
			clearChanged();
		} finally {
			lock.unlock();
		}
		for (Observer o:observers)
			o.update(this, arg);
	}
}
