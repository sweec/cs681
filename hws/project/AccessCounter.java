package project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccessCounter {
	private HashMap<String, Integer> counts = new HashMap<String, Integer>();
	private HashMap<String, Long> ages = new HashMap<String, Long>();
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean debug = false;
	private static AccessCounter instance = null;
	private AccessCounter() {}
	public static AccessCounter getInstance() {
		if (instance == null) {
			lock.writeLock().lock();
			if (instance == null) {
				AccessCounter inst = new AccessCounter();
				instance = inst;
			}
			lock.writeLock().unlock();
		}
		return instance;
	}
	
	public void increment(String path) {
		lock.writeLock().lock();
		Integer count = counts.get(path);
		if (count == null)
			counts.put(path, 1);
		else
			counts.put(path, count+1);
		ages.put(path, System.currentTimeMillis());
		lock.writeLock().unlock();
	}
	
	public int getCount(String path) {
		lock.readLock().lock();
		Integer count = counts.get(path);
		if (count == null)
			count = 0;
		if (debug)
			System.out.println(path+" is accessed "
					+ count + " times");
		lock.readLock().unlock();
		return count;
	}
	
	public long getAge(String path) {
		lock.readLock().lock();
		Long age = ages.get(path);
		if (age == null)
			age = System.currentTimeMillis();
		lock.readLock().unlock();
		return age;
	}
	
	public void summary(ArrayList<String> paths, ArrayList<Integer> cs, ArrayList<String> ts) {
		lock.readLock().lock();
		for (String key:counts.keySet()) {
			paths.add(key);
			cs.add(counts.get(key));
			ts.add(HttpUtility.getGMT(ages.get(key)));
		}
		lock.readLock().unlock();
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
