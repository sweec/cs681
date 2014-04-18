package hw5;

import java.util.concurrent.locks.ReentrantLock;

public class SecurityGate {
	private int counter = 0;
	private static SecurityGate instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	private SecurityGate() {}
	public static SecurityGate getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				SecurityGate inst = new SecurityGate();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}
	
	public void enter(){
		lock.lock();
		counter++;
		lock.unlock();
	}
	public void exit(){
		lock.lock();
		counter--;
		lock.unlock();
	}
	public int getCount(){
		return counter;
	}

	public static void main(String[] args) {
		for (int i=0;i<5;i++)
			new Thread(new Guest()).start();
	}
}
