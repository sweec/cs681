package project;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserInfo {
	private HashSet<String> users = new HashSet<String>();
	private UserInfo(){}
	private ReentrantReadWriteLock lockRW = new ReentrantReadWriteLock();
	private static UserInfo instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static UserInfo getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				UserInfo inst = new UserInfo();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}
	
	public void addUser(String user) {
		lockRW.writeLock().lock();
		users.add(user);
		lockRW.writeLock().unlock();
	}
	
	public boolean hasBasicUser(String user) {
		lockRW.readLock().lock();
		try {
			return users.contains(user);
		} finally {
			lockRW.readLock().unlock();
		}
	}
	
	public boolean hasDigestUser(String user) {
		lockRW.readLock().lock();
		try {
			return users.contains(user);
		} finally {
			lockRW.readLock().unlock();
		}
	}
}
