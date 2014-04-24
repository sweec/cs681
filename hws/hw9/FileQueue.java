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
	
	public void put(File file) {
		lock.lock();
		while (queue.size() >= MaxQueueSize)
			try {
				queueNotFull.await();
			} catch (InterruptedException e) {
				System.out.println("Add file to queue is stopped");
				break;
			}
		if (queue.size() < MaxQueueSize) {
			queue.add(file);
			queueNotEmpty.signalAll();
		}
		lock.unlock();
	}
	
	public File get() {
		File file = null;
		lock.lock();
		while (queue.isEmpty())
			try {
				queueNotEmpty.await();
			} catch (InterruptedException e) {
				System.out.println("Get file from queue is stopped");
				break;
			}
		if (!queue.isEmpty()) {
			file = queue.remove(0);
			queueNotFull.signalAll();
		}
		lock.unlock();
		return file;
	}
}
