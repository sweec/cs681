package hw3;

import java.util.concurrent.locks.ReentrantLock;

public class SummationRunnable implements Runnable {
	private int upperBound;
	private boolean done = false;
	private ReentrantLock lock = new ReentrantLock();
	
	public SummationRunnable(int n) {
		upperBound = n;
	}
	
	public void setDone() {
		lock.lock();
		done = true;
		lock.unlock();
	}
	
	public void run() {
		try {
			while( true ) {
				lock.lock();
				if (done) {
					lock.unlock();
					break;
				}
				System.out.println(upperBound);
				upperBound--;
				lock.unlock();
				Thread.sleep(1000);
				if( upperBound<0 ) {
					System.out.println("print done");
					return;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Error: "+e.getMessage());
		}
		System.out.println("stopped by main()!");
		done = false;
	}
	
	public static void main(String[] args)
	{
		SummationRunnable sRunnable = new SummationRunnable(10);
		Thread thread = new Thread(sRunnable);
		thread.start();
		
		for(int i=0; i<5; i++) {
			System.out.println("#");
		}
		sRunnable.setDone();
		try
		{
			thread.join();
			new Thread(sRunnable).start();
		} catch (InterruptedException e)
		{
		}
	}
}
