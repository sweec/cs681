package hw4;

import java.util.concurrent.locks.ReentrantLock;

public class SimpleFile {
	private boolean changed = false;
	private int count = 0;
	private int storage = 0;
	private ReentrantLock lock = new ReentrantLock();
	
	public void save() {
		lock.lock();
		if (!changed) {
			System.out.println("Thread "+Thread.currentThread().getId()+": already saved");
			lock.unlock();
			return;
		}
		changed = false;
		lock.unlock();
		System.out.println("Thread "+Thread.currentThread().getId()+": save "+count);
		storage = count;
	}
	
	public void change() {
		count = storage + 1;
		System.out.println("change "+count);
		lock.lock();
		changed = true;
		lock.unlock();
	}
	
	public static void main(String[] args) {
		SimpleFile file = new SimpleFile();
		Thread editor = new Thread(new SimpleEditor(file));
		editor.start();
		Thread saver = new Thread(new AutoSaver(file));
		saver.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		editor.interrupt();
		saver.interrupt();
	}
}
