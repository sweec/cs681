package hw11;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccessCounter {
	private HashMap<String, Integer> map = new HashMap<String, Integer>();
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean debug = false;
	
	public void increment(String path) {
		lock.writeLock().lock();
		Integer count = map.get(path);
		if (count == null)
			map.put(path, 1);
		else
			map.put(path, count+1);
		lock.writeLock().unlock();
	}
	
	public int getCount(String path) {
		lock.readLock().lock();
		Integer count = map.get(path);
		if (count == null)
			count = 0;
		if (debug)
			System.out.println(path+" is accessed "
					+ count + " times");
		lock.readLock().unlock();
		return count;
	}
	
	public static void main(String[] args) {
		final AccessCounter ac = new AccessCounter();
		ac.debug = true;
		final Random g = new Random();
		final int PathNum = 10;
		int ThreadNum = 5;
		Thread wths[] = new Thread[ThreadNum];
		Thread rths[] = new Thread[ThreadNum];
		for (int i=0;i<ThreadNum;i++) {
			wths[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						ac.increment("path "+g.nextInt(PathNum));
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
				
			});
			rths[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						ac.getCount("path "+g.nextInt(PathNum));
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
				
			});
			wths[i].start();
			rths[i].start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		for (int i=0;i<ThreadNum;i++) {
			wths[i].interrupt();
			rths[i].interrupt();
		}
	}
}
