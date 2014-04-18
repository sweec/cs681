package hw4;

import java.util.concurrent.locks.ReentrantLock;

public class Singleton {
	private Singleton(){};
	private static Singleton instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static Singleton getInstance(){
		lock.lock();
		if(instance==null) {
			Singleton inst = new Singleton();
			instance = inst;
		}
		lock.unlock();
		return instance;
	}

	public static void main(String[] args) {
		for (int i=0;i<5;i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println(Singleton.getInstance());
				}
				
			}).start();
		}
	}
}
