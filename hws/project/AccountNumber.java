package project;

import java.util.concurrent.locks.ReentrantLock;

public class AccountNumber {
	private static int curNum = 0;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static int generate() {
		int newNum = 0;
		lock.lock();
		curNum++;
		newNum = curNum;
		lock.unlock();
		return newNum;
	}
}
