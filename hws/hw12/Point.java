package hw12;

import java.util.concurrent.locks.ReentrantLock;

public class Point {
	private int state = -1;
	private boolean occupied = false;
	private ReentrantLock lock = new ReentrantLock();
	
	public boolean isOccupied() {
		lock.lock();
		try {
			return occupied;
		} finally {
			lock.unlock();
		}
	}
	
	public void changeState(int id) {
		lock.lock();
		if (state != id)	// simulate railway switch
			state = id;
		occupied = true;
		lock.unlock();
	}
	
	public void clear() {
		occupied = false;
	}
}
