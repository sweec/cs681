package project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.DatatypeConverter;

public class UserInfo {
	private HashSet<String> basicUsers = new HashSet<String>();
	private HashMap<String, String> digestUsers = new HashMap<String, String>();
	private UserInfo(){}
	private ReentrantReadWriteLock basiclock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock digestlock = new ReentrantReadWriteLock();
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
	
	public void addBasicUser(String user) {
		basiclock.writeLock().lock();
		basicUsers.add(user);
		basiclock.writeLock().unlock();
	}
	
	public void addBasicUser(String username, String password) {
		String user = username+":"+password;
		addBasicUser(DatatypeConverter.printBase64Binary(user.getBytes()));
	}
	
	public boolean hasBasicUser(String user) {
		basiclock.readLock().lock();
		try {
			return basicUsers.contains(user);
		} finally {
			basiclock.readLock().unlock();
		}
	}
	
	private void addDigestUser(String username, String info) {
		digestlock.writeLock().lock();
		digestUsers.put(username, info);
		digestlock.writeLock().unlock();
	}
	
	public void addDigestUser(String username, String password, String realm) {
		String info = username+":"+realm+":"+password;
		addDigestUser(username, HttpUtility.toMD5(info));
	}
	
	public String getDigestUser(String username) {
		digestlock.readLock().lock();
		try {
			return digestUsers.get(username);
		} finally {
			digestlock.readLock().unlock();
		}
	}
}
