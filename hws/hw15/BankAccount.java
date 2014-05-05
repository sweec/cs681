package hw15;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BankAccount {
	private double balance = 0;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	public double getBalance() {
		lock.readLock().lock();
		try {
			return balance;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public double deposit( double amount) {
		lock.writeLock().lock();
		try {
			balance += amount;
			return balance;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public double withdraw( double amount) {
		lock.writeLock().lock();
		try {
			balance -= amount;
			return balance;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
}
