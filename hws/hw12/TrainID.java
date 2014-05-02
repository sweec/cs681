package hw12;

import java.util.concurrent.locks.ReentrantLock;

public class TrainID {
	private static int curID = 0;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static int generate() {
		int newID = 0;
		lock.lock();
		curID++;
		newID = curID;
		lock.unlock();
		return newID;
	}
}
