package hw9;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FileQueue {
	private ArrayList<File> queue = new ArrayList<File>();
	private int MaxQueueSize = 10;
	private ReentrantLock lock = new ReentrantLock();
	private Condition queueNotEmpty = lock.newCondition();
	private Condition queueNotFull = lock.newCondition();
	private boolean done = false;
	
	/**
	 * release any threads that are still waiting
	 * @param done
	 */
	public void setDone(boolean done) {
		lock.lock();
		this.done = done;
		queueNotEmpty.signalAll();
		queueNotFull.signalAll();
		lock.unlock();
	}
	
	public void put(File file) {
		lock.lock();
		while (queue.size() >= MaxQueueSize && !done)
			try {
				queueNotFull.await();
			} catch (InterruptedException e) {
			}
		if (!done) {
			queue.add(file);
			queueNotEmpty.signalAll();
		}
		lock.unlock();
	}
	
	public File get() {
		File file = null;
		lock.lock();
		while (queue.isEmpty() && !done)
			try {
				queueNotEmpty.await();
			} catch (InterruptedException e) {
			}
		if (!done) {
			file = queue.get(0);
			queue.remove(0);
			queueNotFull.signalAll();
		}
		lock.unlock();
		return file;
	}
}
